package nitis.gravillaso.world.blocks.distribution;

import arc.Core;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.distribution.Conveyor.*;
import mindustry.world.blocks.distribution.StackConveyor.*;
import mindustry.world.meta.*;
import nitis.gravillaso.GravillasoMod;
import nitis.gravillaso.annotations.Annotations.*;
import nitis.gravillaso.graphics.*;

import static mindustry.Vars.*;

public class DistributionLine extends Block implements Autotiler{
    protected static final int
    stateMove = 0, // move items forward (move forward)
    stateLoad = 1, // load items (input from building)
    stateFork = 2, // when several lines come from current block, this block become a fork a.k.a. router (1+ input, 2+ output)
    stateJunction = 3, // works like move, but alternately changing direction (has 2 accept sides, and 2 output; only left ↔ right, top ↔ bottom)
    stateUnload = 4;

    public @Load(value = "@name$-bottom") TextureRegion bottomRegion;
    public @Load("@name$-edge") TextureRegion edgeRegion;
    public @Load("@name$-stack") TextureRegion stackRegion;

    public float speed = 0f;
    public float recharge = 3f;

    public Color lineColor = Pal.accent;
    public Effect loadEffect = Fx.conveyorPoof;
    public Effect unloadEffect = Fx.conveyorPoof;

    public DistributionLine(String name){
        super(name);

        rotate = true;
        update = true;
        group = BlockGroup.transportation;
        hasItems = true;
        itemCapacity = 10;
        conveyorPlacement = true;
        underBullets = true;
        priority = TargetPriority.transport;
        drawCached = true;
        buildingCacheLayer = BuildingCacheLayer.under;

        ambientSound = Sounds.loopConveyor;
        ambientSoundVolume = 0.004f;
    }

    @Override
    public String getDisplayName(Tile tile){
        if(tile.build instanceof DistributionLineBuild build){
            return super.getDisplayName(tile) + " " + build.state + " " + build.blendprox + " " + build.cooldown;
        }

        return super.getDisplayName(tile);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.itemsMoved, Mathf.round(itemCapacity * speed * 60), StatUnit.itemsSecond);
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
        if(tile.build instanceof DistributionLineBuild b){
            int state = b.state;
            if(state == stateLoad){ //standard conveyor mode
                return otherblock.outputsItems() && lookingAtEither(tile, rotation, otherx, othery, otherrot, otherblock);
            }else if(state == stateUnload){ //router mode
                return otherblock.acceptsItems &&
                (!otherblock.noSideBlend || lookingAtEither(tile, rotation, otherx, othery, otherrot, otherblock)) &&
                (notLookingAt(tile, rotation, otherx, othery, otherrot, otherblock) || (otherblock instanceof DistributionLine && facing(otherx, othery, otherrot, tile.x, tile.y))) &&
                !(world.build(otherx, othery) instanceof DistributionLineBuild s && s.state == stateUnload) &&
                !(world.build(otherx, othery) instanceof DistributionLineBuild s2 && s2.state == stateMove &&
                !facing(otherx, othery, otherrot, tile.x, tile.y));
            }else if(state == stateFork || state == stateJunction){
                return otherblock.outputsItems() && (otherblock instanceof DistributionLine || lookingAtEither(tile, rotation, otherx, othery, otherrot, otherblock));
            }
        }

        if(otherblock instanceof DistributionLine && world.build(otherx, othery) instanceof DistributionLineBuild other && (other.state == stateFork || other.state == stateJunction)){
            return otherblock.outputsItems();
        }

        return otherblock.outputsItems() && blendsArmored(tile, rotation, otherx, othery, otherrot, otherblock) && otherblock instanceof DistributionLine;
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        int[] bits = getTiling(plan, list);

        if(bits == null) return;

        Draw.rect(bottomRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);

        for(int i = 0; i < 4; i++){
            if((bits[3] & (1 << i)) == 0){
                Draw.rect(edgeRegion, plan.drawx(), plan.drawy(), (plan.rotation - i) * 90);
            }
        }
    }

    @Override
    public boolean rotatedOutput(int x, int y){
        Building tile = world.build(x, y);
        if(tile instanceof DistributionLineBuild s){
            return s.state != stateUnload;
        }
        return super.rotatedOutput(x, y);
    }

    public class DistributionLineBuild extends Building{
        public int state, blendprox, outputMask;

        public int link = -1, outputCursor, routeDir = -1;
        public float cooldown;
        public Item lastItem;

        boolean proxUpdating = false;

        @Override
        public void drawCached(){
            Draw.rect(bottomRegion, x, y, rotdeg());

            for(int i = 0; i < 4; i++){
                if((blendprox & (1 << i)) == 0){
                    Draw.rect(edgeRegion, x, y, (rotation - i) * 90);
                }
            }

            drawLines();

            //draw inputs
            if(state == stateLoad){
                for(int i = 0; i < 4; i++){
                    int dir = Mathf.mod(rotation - i, 4);
                    var near = nearby(dir);
                    if((blendprox & (1 << i)) != 0 && i != 0 && near != null && !near.block.squareSprite){
                        Draw.rect(sliced(bottomRegion, SliceMode.bottom), x + Geometry.d4x(dir) * tilesize*0.75f, y + Geometry.d4y(dir) * tilesize*0.75f, (float)(dir*90));
                    }
                }
            }else if(state == stateUnload){ //front unload
                //TOOD hacky front check
                if((blendprox & (1)) != 0 && front() != null && !front().block.squareSprite){
                    Draw.rect(sliced(bottomRegion, SliceMode.top), x + Geometry.d4x(rotation) * tilesize*0.75f, y + Geometry.d4y(rotation) * tilesize*0.75f, rotation * 90f);
                }
            }
        }

        @Override
        public void draw(){
            Draw.z(Layer.block - 0.1f);

            Tile from = world.tile(link);

            if(link == -1 || from == null || lastItem == null) return;

            int fromRot = rotation;
            if(from.build != null){
                for(int i = 0; i < 4; i++){
                    if(nearby(i) == from.build){
                        fromRot = (i + 2) & 3;
                        break;
                    }
                }
            }

            //offset
            Tmp.v1.set(from.worldx(), from.worldy());
            Tmp.v2.set(x, y);
            Tmp.v1.interpolate(Tmp.v2, 1f - cooldown, Interp.linear);

            //rotation
            int targetRot = (state == stateFork || state == stateJunction) && routeDir != -1 ? routeDir : rotation;
            float a = (fromRot%4) * 90;
            float b = (targetRot%4) * 90;
            if((fromRot%4) == 3 && (targetRot%4) == 0) a = -1 * 90;
            if((fromRot%4) == 0 && (targetRot%4) == 3) a =  4 * 90;

            //stack
            Draw.rect(stackRegion, Tmp.v1.x, Tmp.v1.y, Mathf.lerp(a, b, Interp.smooth.apply(1f - Mathf.clamp(cooldown * 2, 0f, 1f))));

            //item
            float size = itemSize * Mathf.lerp(Math.min((float)items.total() / itemCapacity, 1), 1f, 0.4f);
            Draw.rect(lastItem.fullIcon, Tmp.v1.x, Tmp.v1.y, size, size, 0);
        }

        protected void drawLines(){
            Lines.stroke(1f, lineColor);

            final float halfSize = tilesize / 2f;
            final float revPadding = tilesize / 4f;
            if(state != stateFork){
                for(int i = 0; i < 4; i++){
                    int dir = Mathf.mod(rotation - i, 4);
                    float xOffset = Geometry.d4x(dir) * halfSize;
                    float yOffset = Geometry.d4y(dir) * halfSize;
                    if((blendprox & (1 << i)) != 0){
                        Lines.line(x, y, x + xOffset, y + yOffset);
                    }
                }
            }else{
                for(int i = 0; i < 4; i++){
                    int dir = Mathf.mod(rotation - i, 4);
                    float xOffset = Geometry.d4x(dir) * halfSize;
                    float yOffset = Geometry.d4y(dir) * halfSize;
                    float padX = Geometry.d4x(dir) * (revPadding + 0.225f); //little offset between square and lines required
                    float padY = Geometry.d4y(dir) * (revPadding + 0.225f);
                    if((blendprox & (1 << i)) != 0){
                        Lines.line(x + padX, y + padY, x + xOffset, y + yOffset);
                    }
                }
            }

            switch(state){
                case stateFork -> {
                    Lines.beginLine();
                    if(Integer.bitCount(blendprox) == 4){
                        for(int i = 0; i < 4; i++){
                            float xOffset = Geometry.d4x[i] * revPadding;
                            float yOffset = Geometry.d4y[i] * revPadding;
                            Lines.linePoint(x + xOffset, y + yOffset);
                        }
                        Lines.endLine(true);
                    }else{ //else, only 3 bits
                        //it's hard to explain
                        //tldr: returns first index of zero
                        int forkDir = Integer.numberOfTrailingZeros(~blendprox & 0xF) + rotation + (blendprox == 11 ? 2 : 0); // 11 is a very magic number
                        int left = forkDir - 1;
                        int right = forkDir + 1;

                        float xOffset = Geometry.d4x(left) * revPadding;
                        float yOffset = Geometry.d4y(left) * revPadding;
                        Lines.linePoint(x + xOffset, y + yOffset);
                        xOffset = Geometry.d4x(forkDir) * revPadding;
                        yOffset = Geometry.d4y(forkDir) * revPadding;
                        Lines.linePoint(x + xOffset, y + yOffset);
                        xOffset = Geometry.d4x(right) * revPadding;
                        yOffset = Geometry.d4y(right) * revPadding;
                        Lines.linePoint(x + xOffset, y + yOffset);
                        //yeah, it's unwrapped loop, go ahead, BLAME ME!!

                        Lines.endLine(false);
                    }
                }
                case stateJunction -> {
                    Lines.line(x - revPadding, y - revPadding, x + revPadding, y + revPadding);
                    Lines.line(x + revPadding, y - revPadding, x - revPadding, y + revPadding);
                }
                case stateLoad -> {
                    Draw.rect("white", x, y, 2.5f, 2.5f, 45f);
                }
                case stateUnload -> {
                    Draw.rect("white", x, y, 2.5f, 2.5f, 0f);
                }
            }
            Draw.color();
        }

        @Override
        public void dropped(){
            super.dropped();
            var prev = Geometry.d4[(rotation + 2) % 4];
            if(items.any()){
                link = Point2.pack(tile.x + prev.x, tile.y + prev.y);
                cooldown = 0f;
            }else{
                link = -1;
            }
        }

        @Override
        public void drawCracks(){
            Draw.z(Layer.block - 0.15f);
            super.drawCracks();
        }

        @Override
        public void payloadDraw(){
            Draw.rect(block.fullIcon, x, y);
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            recache();

            int lastState = state;

            state = stateMove;

            int[] bits = buildBlending(tile, rotation, null, true);
            if(bits[0] == 0 && blends(tile, rotation, 0) && (!blends(tile, rotation, 2) || back() instanceof DistributionLineBuild b && b.state == stateUnload)) state = stateLoad;  // a 0 that faces into a conveyor with none behind it
            if(!(front() instanceof DistributionLineBuild)) state = stateUnload; // a 0 that faces into none with a conveyor behind it

            // TODO: This algorithm is a shit, need to come up with something better
            int inputs = 0, inputMask = 0;
            int outputs = 0;
            outputMask = 0;
            for(int i = 0; i < 4; i++){
                if(nearby(i) instanceof DistributionLineBuild line && line.team == team && line.isValid()){
                    if(line.front() == this){
                        inputs++;
                        inputMask |= 1 << i;
                    }else if (line.back() == this || this.front() == line){
                        outputs++;
                        outputMask |= 1 << i;
                    }
                }
            }

            boolean oppositeInputs =  (inputMask & 0b0101) == 0b0101 || (inputMask & 0b1010) == 0b1010 ;
            if(inputs >= 2 && outputs >= 2 && !oppositeInputs){
                state = stateJunction;
            }else if(inputs >= 1 && outputs >= 2){
                Log.debug("state fork @x@, i@, o@", x, y, inputs, outputs);
                state = stateFork;
            }

            if(!headless){
                blendprox = 0;

                for(int i = 0; i < 4; i++){
                    if(blends(tile, rotation, i) && (state != stateUnload || i == 0 || nearby(Mathf.mod(rotation - i, 4)) instanceof DistributionLineBuild)){
                        blendprox |= (1 << i);
                    }
                }
            }

            if(state == stateLoad){
                for(Building near : proximity){
                    if(near instanceof DistributionLineBuild && near.front() == this){
                        state = stateMove;
                        break;
                    }
                }
            }

            if(state != lastState){
                proxUpdating = true;
                for(Building near : proximity){
                    if(!(near instanceof DistributionLineBuild b && b.proxUpdating && b.state != stateUnload)){
                        near.onProximityUpdate();
                    }
                }
                proxUpdating = false;
            }
        }

        public int getLink(){
            return link;
        }

        protected boolean canTransferTo(Building target){
            if(target == null || target.team != team || lastItem == null) return false;
            if(target instanceof DistributionLineBuild line) return line.getLink() == -1;
            if(target instanceof StackConveyorBuild line) return line.link == -1;
            return false;
        }

        protected void transferTo(Building target){
            if(target instanceof DistributionLineBuild line){
                if(!canTransferTo(line)) return;
                line.items.add(items);
                line.lastItem = lastItem;
                line.link = tile.pos();
                link = -1;
                items.clear();
                cooldown = recharge;
                line.cooldown = 1;
                return;
            }

            if(target instanceof StackConveyorBuild stackConveyor){
                if(!canTransferTo(stackConveyor)) return;
                stackConveyor.items.add(items);
                stackConveyor.lastItem = lastItem;
                stackConveyor.link = tile.pos();
                link = -1;
                items.clear();
                cooldown = recharge;
                stackConveyor.cooldown = 1;
            }

        }

        protected int selectJunctionOutput(){
            if(link == -1) return -1;
            Tile from = world.tile(link);
            if(from == null || from.build == null) return -1;

            for(int i = 0; i < 4; i++){
                if(nearby(i) == from.build) return (i + 2) & 0b11;
            }
            return -1;
        }

        protected int selectForkOutput(){
            int start = outputCursor & 0b11;
            for(int i = 0; i < 4; i++){
                int dir = (start + i) & 0b11;
                if((outputMask & (1 << dir)) == 0) continue;
                if(canTransferTo(nearby(dir))) return dir;
            }
            return -1;
        }

        protected boolean transferOutput(int direction){
            if(direction < 0) return false;
            transferTo(nearby(direction));
            outputCursor = (direction + 1) & 0b11;
            routeDir = -1;
            return true;
        }

        @Override
        public void updateTile(){
            //the item still needs to be "reeled" in when disabled
            float eff = enabled ? efficiency : 1f;

            //reel in crater
            if(cooldown > 0f){
                cooldown = Mathf.clamp(cooldown - speed * eff * delta(), 0f, recharge);
                //this is pure visuals
                if(link != -1 && (state == stateFork || state == stateJunction) && routeDir == -1){
                    routeDir = state == stateFork ? selectForkOutput() : selectJunctionOutput();
                }
            }

            //indicates empty state
            if(link == -1) return;

            //crater needs to be centered
            if(cooldown > 0f) return;

            //get current item
            if(lastItem == null || !items.has(lastItem)){
                lastItem = items.first();
            }

            //do not continue if disabled, will still allow one to be reeled in to prevent visual stacking
            if(!enabled) return;

            if(state == stateUnload){ //unload
                while(lastItem != null ? moveForward(lastItem) : dump(null)){
                    items.remove(lastItem, 1);

                    if(!items.has(lastItem)){
                        poofOut();
                        lastItem = null;
                        break;
                    }
                }
            }else if(state == stateFork){
                int dir = selectForkOutput();
                transferOutput(dir);
            }else if(state == stateJunction){
                int dir = selectJunctionOutput();
                transferOutput(dir);
            }else{ //transfer
                if(state != stateLoad || (items.total() >= getMaximumAccepted(lastItem))){
                    transferTo(front());
                }
            }
        }

        @Override
        public boolean canUnload(){
            return state != stateLoad;
        }

        @Override
        public void overwrote(Seq<Building> builds){
            if(builds.first() instanceof ConveyorBuild build){
                Item item = build.items.first();
                if(item != null){
                    handleStack(item, build.items.get(item), null);
                }
            }
        }

        @Override
        public boolean shouldAmbientSound(){
            return false; //has no moving parts;
        }

        protected void poofIn(){
            link = tile.pos();
            routeDir = -1;
            loadEffect.at(this);
        }

        protected void poofOut(){
            unloadEffect.at(this);
            link = -1;
            routeDir = -1;
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            if(items.any() && !items.has(item)) return 0;
            return super.acceptStack(item, amount, source);
        }

        @Override
        public void handleItem(Building source, Item item){
            if(items.empty() && tile != null){
                poofIn();
            }

            super.handleItem(source, item);
            lastItem = item;
        }

        @Override
        public void handleStack(Item item, int amount, @Nullable Teamc source){
            if(amount <= 0) return;
            if(items.empty() && tile != null){
                poofIn();
            }

            super.handleStack(item, amount, source);
            lastItem = item;
        }

        @Override
        public int removeStack(Item item, int amount){
            try{
                return super.removeStack(item, amount);
            }finally{
                if(items.empty()){
                    poofOut();
                }
            }
        }

        @Override
        public void itemTaken(Item item){
            if(items.empty()){
                poofOut();
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            if (this == source) {
                return items.total() < itemCapacity && (!items.any() || items.has(item));
            }

            boolean fromLine = source instanceof DistributionLineBuild line &&
            (line.front() == this || (back() == line && (line.state == stateFork || line.state == stateJunction)));

            return cooldown <= recharge - 1f
            && source.team == team
            && (state == stateLoad || fromLine)
            && (!items.any() || items.has(item))
            && items.total() < getMaximumAccepted(item)
            && source != front();
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.i(link);
            write.b(outputCursor);
            write.f(cooldown);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            link = read.i();
            outputCursor = read.b();
            cooldown = read.f();
            lastItem = items.first();
        }
    }
}
