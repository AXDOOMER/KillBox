import java.util.ArrayList;

public class Sound
{
    final int SFX_VOICES = 32;
    boolean Preload_ = false;

    public Sound(boolean Preload, ArrayList<Player> Listeners)
    {
        System.out.print("SFX initialisation");

        Preload_ = Preload;
        if(Preload_)
        {
            // Load sound effects in memory
            System.out.print(" (with precache)");
        }

        ArrayList<Player> Players = Listeners;

        System.out.println(". ");
    }

    public Boolean PlaySound(Player Source, String Name)
    {
        // Get the sound from a Player and the source will be at its X,Y,Z.


        // If the sound was not found, return false:
        return false;
    }
}
