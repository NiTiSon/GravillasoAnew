package nitis.gravillaso.content;

import mindustry.type.*;

import static nitis.gravillaso.content.GrPlanets.gravillo;

public class GrSectorPresets{
    public static SectorPreset init;

    public static void load(){
        init = new SectorPreset("init", gravillo, 32){{
            alwaysUnlocked = true;
            difficulty = 1;
        }};
    }
}
