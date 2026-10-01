package nitis.gravillaso.graphics;

import arc.files.*;
import arc.graphics.gl.*;
import arc.util.*;
import mindustry.graphics.*;
import nitis.gravillaso.*;

import static mindustry.Vars.*;

public class GrShaders{
    public static RainbowShader rainbow;

    public static void init(){
        rainbow = new RainbowShader(vanilla("default.vert"), mod("rainbow.frag"));
    }

    // TODO: remove
    public static class RainbowShader extends Shader{
        public RainbowShader(Fi vertexShader, Fi fragmentShader){
            super(vertexShader, fragmentShader);
        }

        @Override
        public void apply(){
            setUniformf("u_time", Time.time);
        }
    }

    public static Fi mod(String name){
        Fi root = mods.getMod(GravillasoMod.class).root;
        return root.child("shaders").child(name);
    }

    public static Fi vanilla(String name){
        return Shaders.getShaderFi(name);
    }
}
