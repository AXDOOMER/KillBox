// Copyright (C) 2026 Alexandre-Xavier Labonté-Lamoureux
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import java.util.ArrayList;
import java.util.Random;

/**
 * Bot — a CPU-controlled player.
 *
 * The bot controls its underlying Player exclusively through the same public
 * methods a human uses: ForwardMove, LateralMove, AngleTurn, SetShotTrue.
 * This keeps bot commands compatible with future network serialisation.
 *
 * Skill adaptation
 * ----------------
 * "skill" is a float in [0, 1].  Higher values mean better aim, faster
 * reaction and more aggression.  The bot adjusts its target skill every
 * SKILL_LAG_MIN–SKILL_LAG_MAX ticks based on the kill/death ratio, with a
 * small random variety offset so that the adjustment feels human-like rather
 * than mechanical.
 *
 * Pathfinding
 * -----------
 * A full nav-mesh is not available in this engine, so the bot uses a
 * visibility-based approach:
 *   - If it can see the opponent (ray-cast that avoids walls), it chases
 *     and attacks.
 *   - Otherwise it moves toward the last known position, then wanders.
 *   - Stuck detection rotates the wander direction when the bot stops making
 *     progress.
 */
public class Bot
{
	// -----------------------------------------------------------------------
	// Core references
	// -----------------------------------------------------------------------

	private final Player self;
	private final Level  level;
	private final int    gameMode;   // 0 = FFA, 1 = One-Shot-Kill, 2 = Flag Tag

	// -----------------------------------------------------------------------
	// AI state machine
	// -----------------------------------------------------------------------

	private enum State { WANDER, INVESTIGATE, ATTACK, GET_WEAPON, GET_FLAG }
	private State state = State.WANDER;

	// -----------------------------------------------------------------------
	// Target tracking
	// -----------------------------------------------------------------------

	private float   lastKnownX   = 0;
	private float   lastKnownY   = 0;
	private boolean hasLastKnown = false;
	private int     noSightTicks = 0;

	/** Ticks before forgetting the last known position (~8 s at 30 fps). */
	private static final int FORGET_TICKS = 240;

	/** Reaction-delay countdown after spotting the opponent. */
	private int     reactionCountdown     = 0;
	private boolean opponentJustSpotted   = false;

	// -----------------------------------------------------------------------
	// Skill adaptation
	// -----------------------------------------------------------------------

	private float skill        = 0.5f;   // current effective skill
	private float targetSkill  = 0.5f;   // skill we are converging toward
	private int   skillLagLeft = 0;       // ticks until next adaptation check
	private float skillVariety = 0f;      // random ±offset for realism

	private static final int SKILL_LAG_MIN =  60;  // min 2 s between checks
	private static final int SKILL_LAG_MAX = 270;  // max 9 s between checks

	// -----------------------------------------------------------------------
	// Wander / navigation
	// -----------------------------------------------------------------------

	private float wanderAngle = 0f;
	private int   wanderTimer = 0;
	private static final int WANDER_HOLD = 60;   // ticks per wander segment

	// -----------------------------------------------------------------------
	// Stuck detection
	// -----------------------------------------------------------------------

	private float prevX     = 0;
	private float prevY     = 0;
	private int   stuckTicks = 0;
	private static final int STUCK_LIMIT = 40;

	// -----------------------------------------------------------------------
	// Combat / movement
	// -----------------------------------------------------------------------

	private int   strafeDir   = 0;   // -1, 0, +1
	private int   strafeTimer = 0;

	// -----------------------------------------------------------------------
	private final Random rng;

	// =======================================================================
	// Constructor
	// =======================================================================

	public Bot(Player botPlayer, Level level, int gameMode)
	{
		this.self     = botPlayer;
		this.level    = level;
		this.gameMode = gameMode;
		this.rng      = new Random();
		this.wanderAngle = rng.nextFloat() * (float)(2 * Math.PI);
		this.prevX = botPlayer.PosX();
		this.prevY = botPlayer.PosY();
	}

	// =======================================================================
	// Main update — called once per game tick
	// =======================================================================

	public void update(ArrayList<Player> players)
	{
		if (self.Health <= 0) return;   // dead — Game.java handles the respawn

		Player human = findHumanPlayer(players);
		if (human == null) return;

		// 1. Skill adaptation
		adaptSkill(human);
		smoothSkill();

		// 2. Visibility check
		boolean canSee = human.Health > 0 && hasLineOfSight(human);

		if (canSee)
		{
			if (!opponentJustSpotted)
			{
				opponentJustSpotted = true;
				reactionCountdown   = reactionDelay();
			}
			if (reactionCountdown > 0)
			{
				reactionCountdown--;
				canSee = false;   // still in reaction window — pretend we can't see
			}
			else
			{
				lastKnownX   = human.PosX();
				lastKnownY   = human.PosY();
				hasLastKnown = true;
				noSightTicks = 0;
			}
		}
		else
		{
			opponentJustSpotted = false;
			reactionCountdown   = 0;
			noSightTicks++;
			if (noSightTicks > FORGET_TICKS)
			{
				hasLastKnown = false;
			}
		}

		// 3. Choose state
		if (gameMode == 2)
		{
			chooseFlagTagState(canSee, human);
		}
		else if (canSee)
		{
			state = State.ATTACK;
		}
		else if (hasLastKnown)
		{
			state = State.INVESTIGATE;
		}
		else
		{
			// Head for an uncollected weapon if one exists and we are still on the pistol
			Thing weapon = findBetterWeapon();
			state = (weapon != null && self.SelectedWeapon == 1) ? State.GET_WEAPON : State.WANDER;
		}

		// 4. Execute state
		switch (state)
		{
			case WANDER:      doWander();              break;
			case INVESTIGATE: doInvestigate();         break;
			case ATTACK:      doAttack(human);         break;
			case GET_WEAPON:  doGetWeapon();           break;
			case GET_FLAG:    doGetFlag();             break;
		}

		// 5. Auto-reload when empty
		if (self.Bullets == 0 && self.CanShot())
		{
			self.Reloading = true;
		}

		// 6. Track position for stuck detection
		prevX = self.PosX();
		prevY = self.PosY();
	}

	// =======================================================================
	// State behaviours
	// =======================================================================

	private void doWander()
	{
		wanderTimer++;
		if (wanderTimer >= WANDER_HOLD || isStuck())
		{
			wanderTimer  = 0;
			stuckTicks   = 0;
			wanderAngle += (float)((rng.nextDouble() - 0.5) * Math.PI * 1.5);
			wanderAngle  = normalizeAngle(wanderAngle);
		}

		// Deflect from walls ahead
		float ahead  = self.GetRadianAngle();
		float checkX = self.PosX() + (float)(Math.cos(ahead) * 48);
		float checkY = self.PosY() + (float)(Math.sin(ahead) * 48);
		if (self.CheckWallCollision(checkX, checkY, 20f, false) != null)
		{
			wanderAngle += (float)((rng.nextBoolean() ? 1 : -1) * Math.PI / 2);
			wanderAngle  = normalizeAngle(wanderAngle);
			stuckTicks   = STUCK_LIMIT;
		}

		// Navigate toward a point far away in the wander direction
		float tx = self.PosX() + (float)(Math.cos(wanderAngle) * 256);
		float ty = self.PosY() + (float)(Math.sin(wanderAngle) * 256);
		navigateTo(tx, ty, 30f);
	}

	private void doInvestigate()
	{
		if (!hasLastKnown)
		{
			state = State.WANDER;
			return;
		}
		if (dist2D(self.PosX(), self.PosY(), lastKnownX, lastKnownY) < 30f)
		{
			hasLastKnown = false;
			state        = State.WANDER;
			return;
		}
		navigateTo(lastKnownX, lastKnownY, 30f);
	}

	private void doAttack(Player human)
	{
		if (!hasLastKnown && human.Health <= 0)
		{
			state = State.WANDER;
			return;
		}

		// Flag-tag special: if we carry the flag, stay away instead of fighting
		if (gameMode == 2 && self.HasFlag)
		{
			float dx = self.PosX() - human.PosX();
			float dy = self.PosY() - human.PosY();
			float tx = self.PosX() + dx;
			float ty = self.PosY() + dy;
			navigateTo(tx, ty, 0f);
			return;
		}

		float dx   = human.PosX() - self.PosX();
		float dy   = human.PosY() - self.PosY();
		float dist = (float)Math.sqrt(dx * dx + dy * dy);

		// Aim with Gaussian noise scaled by (1 - skill)
		double spread = aimSpread();
		double aimDx  = dx + rng.nextGaussian() * dist * spread;
		double aimDy  = dy + rng.nextGaussian() * dist * spread;

		float desired    = (float)Math.atan2(aimDy, aimDx);
		float curAngle   = self.GetRadianAngle();
		float angleDiff  = normalizeAngle(desired - curAngle);
		float turnSpeed  = turnSpeedPerTick();
		float turn       = Math.max(-turnSpeed, Math.min(turnSpeed, angleDiff));
		// Convert radians → internal angle units  (2π rad = 32768 units)
		self.AngleTurn((short)(turn * 16384f / (float)Math.PI));

		// Shoot when close enough to the target angle
		double aimTolerance = 0.10 + (1.0 - skill) * 0.25;
		if (Math.abs(angleDiff) < aimTolerance && self.CanShot() && self.Bullets > 0)
		{
			self.SetShotTrue();
		}

		// Move: maintain a preferred engagement distance, strafe between shots
		float preferredDist = 160f + (1f - skill) * 240f;
		if (dist > preferredDist + 60f)
		{
			self.ForwardMove((byte)1);
		}
		else if (dist < preferredDist - 60f)
		{
			self.ForwardMove((byte)-1);
		}
		else
		{
			strafeTimer--;
			if (strafeTimer <= 0)
			{
				strafeTimer = 25 + rng.nextInt(35);
				strafeDir   = rng.nextBoolean() ? 1 : -1;
			}
			self.LateralMove((byte)strafeDir);
		}
	}

	private void doGetWeapon()
	{
		Thing weapon = findBetterWeapon();
		if (weapon == null)
		{
			state = State.WANDER;
			return;
		}
		navigateTo(weapon.PosX(), weapon.PosY(), 0f);
	}

	private void doGetFlag()
	{
		Thing flag = findThingOfType(Thing.Names.Flag);
		if (flag == null)
		{
			// Flag was picked up while we were heading for it
			state = State.WANDER;
			return;
		}
		navigateTo(flag.PosX(), flag.PosY(), 0f);
	}

	// =======================================================================
	// Flag-tag state logic
	// =======================================================================

	private void chooseFlagTagState(boolean canSee, Player human)
	{
		if (self.HasFlag)
		{
			// We have the flag — avoid the human
			state = canSee ? State.ATTACK : State.WANDER;
		}
		else
		{
			Thing flag = findThingOfType(Thing.Names.Flag);
			if (flag != null)
			{
				state = State.GET_FLAG;       // flag is on the floor — grab it
			}
			else if (human.HasFlag)
			{
				// Human carries the flag — hunt them down
				state = canSee ? State.ATTACK : (hasLastKnown ? State.INVESTIGATE : State.WANDER);
			}
			else
			{
				state = canSee ? State.ATTACK : (hasLastKnown ? State.INVESTIGATE : State.WANDER);
			}
		}
	}

	// =======================================================================
	// Navigation
	// =======================================================================

	/**
	 * Turns and moves toward (tx, ty).
	 * Stops when within {@code slack} units of the target.
	 */
	private void navigateTo(float tx, float ty, float slack)
	{
		float dx   = tx - self.PosX();
		float dy   = ty - self.PosY();
		float dist = (float)Math.sqrt(dx * dx + dy * dy);
		if (dist < Math.max(24f, slack)) return;

		float desired   = (float)Math.atan2(dy, dx);
		float curAngle  = self.GetRadianAngle();
		float angleDiff = normalizeAngle(desired - curAngle);
		float turnSpeed = turnSpeedPerTick();
		float turn      = Math.max(-turnSpeed, Math.min(turnSpeed, angleDiff));
		self.AngleTurn((short)(turn * 16384f / (float)Math.PI));

		// Only move forward when roughly facing the target
		if (Math.abs(angleDiff) < Math.PI / 2.5)
		{
			self.ForwardMove((byte)1);
		}

		// If stuck, strafe to get unstuck
		if (isStuck())
		{
			stuckTicks = 0;
			self.LateralMove((byte)(rng.nextBoolean() ? 1 : -1));
		}
	}

	// =======================================================================
	// Line-of-sight ray check
	// =======================================================================

	/**
	 * Returns true if no wall blocks the straight line between this bot and
	 * {@code target}.  Uses CheckWallCollision with a small radius to simulate
	 * a bullet ray.
	 */
	private boolean hasLineOfSight(Player target)
	{
		float dx    = target.PosX() - self.PosX();
		float dy    = target.PosY() - self.PosY();
		float dist  = (float)Math.sqrt(dx * dx + dy * dy);
		if (dist < 8f) return true;

		int   steps = Math.max(1, (int)(dist / 16f));
		float stepX = dx / steps;
		float stepY = dy / steps;
		float x     = self.PosX();
		float y     = self.PosY();

		for (int i = 1; i < steps; i++)
		{
			x += stepX;
			y += stepY;
			if (self.CheckWallCollision(x, y, 4f, true) != null)
			{
				return false;
			}
		}
		return true;
	}

	// =======================================================================
	// World search helpers
	// =======================================================================

	/** Returns the human player (the first player that is not {@code self}). */
	private Player findHumanPlayer(ArrayList<Player> players)
	{
		for (Player p : players)
		{
			if (p != self) return p;
		}
		return null;
	}

	/**
	 * Returns the nearest weapon on the map that the bot does not yet own,
	 * preferring stronger weapons.
	 */
	private Thing findBetterWeapon()
	{
		Thing best     = null;
		float bestDist = Float.MAX_VALUE;
		for (Thing t : level.Things)
		{
			boolean wantAk47  = t.Type == Thing.Names.Ak47  && self.OwnedWeapons[3] == null;
			boolean wantTek9  = t.Type == Thing.Names.Tek9  && self.OwnedWeapons[2] == null;
			if (wantAk47 || wantTek9)
			{
				float d = dist2D(self.PosX(), self.PosY(), t.PosX(), t.PosY());
				// Prefer the Ak47 even if it is farther away
				boolean betterType = wantAk47 && (best == null || best.Type != Thing.Names.Ak47);
				if (betterType || (best != null && best.Type == t.Type && d < bestDist))
				{
					bestDist = d;
					best     = t;
				}
				else if (best == null)
				{
					bestDist = d;
					best     = t;
				}
			}
		}
		return best;
	}

	private Thing findThingOfType(Thing.Names type)
	{
		for (Thing t : level.Things)
		{
			if (t.Type == type) return t;
		}
		return null;
	}

	// =======================================================================
	// Skill adaptation
	// =======================================================================

	private void adaptSkill(Player human)
	{
		if (skillLagLeft > 0)
		{
			skillLagLeft--;
			return;
		}

		int total = self.Kills + human.Kills;
		if (total < 3) return;   // not enough data yet

		// Bot win-rate: 0 = never wins, 1 = always wins
		float botWinRate = (float)self.Kills / total;

		// Target 50 % win-rate.  Winning too much → lower skill; losing → raise it.
		float correction = (botWinRate - 0.5f) * 0.15f;
		targetSkill = clamp(targetSkill - correction + skillVariety, 0.1f, 0.9f);

		// Randomise variety and set a new lag window for natural-feeling delays
		skillVariety = (float)((rng.nextDouble() - 0.5) * 0.08);
		skillLagLeft = SKILL_LAG_MIN + rng.nextInt(SKILL_LAG_MAX - SKILL_LAG_MIN);
	}

	/** Smoothly interpolate current skill toward the target. */
	private void smoothSkill()
	{
		if (Math.abs(skill - targetSkill) > 0.005f)
		{
			skill += (targetSkill - skill) * 0.03f;
		}
	}

	// =======================================================================
	// Skill-derived parameters
	// =======================================================================

	/** Gaussian aim-noise amplitude: 0 = perfect, higher = wilder. */
	private double aimSpread()
	{
		return 0.30 * (1.0 - skill) + 0.01;
	}

	/** Ticks to wait before reacting to a spotted opponent. */
	private int reactionDelay()
	{
		return (int)(55 * (1.0 - skill)) + 5;
	}

	/** Maximum angle change per tick, in radians. */
	private float turnSpeedPerTick()
	{
		return 0.07f + skill * 0.18f;
	}

	// =======================================================================
	// Utility
	// =======================================================================

	private boolean isStuck()
	{
		float moved = dist2D(self.PosX(), prevX, self.PosY(), prevY);
		if (moved < 0.8f)
		{
			stuckTicks++;
			return stuckTicks > STUCK_LIMIT;
		}
		stuckTicks = 0;
		return false;
	}

	private float dist2D(float x1, float y1, float x2, float y2)
	{
		float dx = x2 - x1, dy = y2 - y1;
		return (float)Math.sqrt(dx * dx + dy * dy);
	}

	/** Wraps an angle in radians to the range (-π, π]. */
	private float normalizeAngle(float a)
	{
		while (a >  Math.PI) a -= 2f * (float)Math.PI;
		while (a < -Math.PI) a += 2f * (float)Math.PI;
		return a;
	}

	private float clamp(float v, float lo, float hi)
	{
		return Math.max(lo, Math.min(hi, v));
	}
}
