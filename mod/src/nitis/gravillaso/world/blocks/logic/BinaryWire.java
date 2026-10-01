package nitis.gravillaso.world.blocks.logic;

import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.meta.*;

public class BinaryWire extends Block{
    public BinaryWire(String name){
        super(name);
        solid = update = true;
        underBullets = true;
        canOverdrive = false;
        group = BlockGroup.logic;
        envEnabled = Env.any;
    }

    public class BinaryWireBuild extends Building implements LogicSource{
        public boolean logicState;

        public boolean logicState(){
            return logicState;
        }
    }
}
