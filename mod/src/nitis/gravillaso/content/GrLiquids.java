package nitis.gravillaso.content;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.type.*;

public class GrLiquids{
    public static Liquid brine, oxygen, neon;

    public static void load(){
        brine = new Liquid("brine", Color.valueOf("f7bfa6")){{
            viscosity = 0.2f;
            effect = StatusEffects.wet;
            boilPoint = 0.4f;
            gasColor = Color.grays(0.9f);
        }};

        oxygen = new Liquid("oxygen", Color.valueOf("e6f8ff")){{
           gas = true;
        }};

        neon = new Liquid("neon", Color.valueOf("d52525")){{
            gas = true;
        }};
    }
}