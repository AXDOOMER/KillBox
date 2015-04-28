import java.util.*;

public class Plane
{
    ArrayList<Float> Vertices = new ArrayList<Float>();

    String TextureName;
    Boolean TwoSided_ = true;

    public void SetTextureName(String TextureName)
    {
        this.TextureName = TextureName;
    }

    public void AddVertex(float Vertex)
    {
        Vertices.add(Vertex);
    }

    public void Flush()
    {
        Vertices.clear();
    }

    public void TwoSided(Boolean IsTwoSided)
    {
        TwoSided_ = IsTwoSided;
    }
}
