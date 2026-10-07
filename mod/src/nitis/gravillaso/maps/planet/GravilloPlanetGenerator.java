package nitis.gravillaso.maps.planet;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.noise.*;
import mindustry.ai.*;
import mindustry.ai.BaseRegistry.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.maps.generators.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.meta.*;
import nitis.gravillaso.content.*;
import nitis.gravillaso.core.*;
import nitis.gravillaso.world.blocks.environment.*;
import nitis.gravillaso.world.reservoir.*;

import static mindustry.Vars.*;

public class GravilloPlanetGenerator extends PlanetGenerator {
    public float heightScl = 0.8f, octaves = 8, persistence = 0.6f, heightPow = 3f, heightMult = 1.8f;

    public static float arkThresh = 0.28f, arkScl = 0.83f;
    public static int arkSeed = 7, arkOct = 2;
    public static float liqThresh = 0.65f, liqScl = 80f;
    public static float airThresh = 0.15f, airScl = 15;
    public static int snowSeed = 8, snowOct = 2;
    public static float snowScl = 15f, snowMag = 3f;

    Liquid[] reservoirDrop = {Liquids.oil, GrLiquids.brine};

    //Block[] terrain = {Blocks.regolith, Blocks.regolith, Blocks.regolith, Blocks.regolith, Blocks.yellowStone, Blocks.rhyolite, Blocks.rhyolite, Blocks.carbonStone};
    Block[] terrain = {GrBlocks.corundum, GrBlocks.corundum, GrBlocks.corundum, GrBlocks.corundum, GrBlocks.alunite, GrBlocks.purpleStone, GrBlocks.purpleStone, GrBlocks.galena}; // TODO: need another one block

    {
        baseSeed = 2;
        //in v9 defaultLoadout will be moved to the planet properties
        defaultLoadout = GrLoadouts.basicBase;
    }

    @Override
    public float getHeight(Vec3 position){
        return Mathf.pow(rawHeight(position), heightPow) * heightMult;
    }

    @Override
    public void getColor(Vec3 position, Color out){
        Block block = getBlock(position);

        out.set(block.mapColor).a(1f - block.albedo);
    }

    @Override
    public float getSizeScl(){
        return 2000 * 1.015f * 12f / 11f;
    }

    float rawHeight(Vec3 position){
        return Simplex.noise3d(seed, octaves, persistence, 1f/heightScl, 15f + position.x, 15f + position.y, 15f + position.z);
    }

    Block getBlock(Vec3 position){
        float px = position.x, py = position.y, pz = position.z;

        float temp = getTemperature(position);
        float height = rawHeight(position);

        height *= 1.2f;
        height = Mathf.clamp(height);

        Block result = terrain[Mathf.clamp((int)(height * terrain.length), 0, terrain.length - 1)];

        float snowNoise = Simplex.noise3d(seed + snowSeed, snowOct, 0.7f, snowScl, px + 4f, py + 8f, pz + 1f);
        float frost = Mathf.clamp((-0.25f - temp) / 0.4f, 0f, 1f);
        if(temp < -0.5f - (snowNoise - 0.5f) * snowMag * frost){
            if(result == GrBlocks.alunite || result == GrBlocks.purpleStone || result == GrBlocks.corundum){
                return Blocks.ice;
            }else{
                return Blocks.snow;
            }
        }

        if(/*ice < redThresh - noArkThresh &&*/ Ridged.noise3d(seed + arkSeed, px + 2f, py + 8f, pz + 1f, arkOct, arkScl) > arkThresh){
            result = Blocks.shale;
        }

//        if(ice > redThresh){
//            result = Blocks.redStone;
//        }else if(ice > redThresh - 0.4f){
//            //TODO this may increase the amount of regolith, but it's too obvious a transition.
//            result = Blocks.regolith;
//        }

        return result;
    }

    public float getTemperature(Vec3 position){
        return getTemperature(position.y, position.z);
    }

    public float getTemperature(float longitude, float latitude){
        return Mathf.map(Math.abs(longitude), 0f, 1f, 0.1f, -0.9f);
    }

    @Override
    public void addWeather(Sector sector, Rules rules) {
        GrRules gr = GrRules.getFrom(rules);
        gr.baseTemperature = getTemperature(sector.tile.v.y, sector.tile.v.x);
        gr.appendTo(rules);
        rules.weather.clear();

        float y = (sector.tile.v.y + 1f) / 2;
        if (y > 0.74f) { // poles has endless snowstorm
            rules.weather.add(new Weather.WeatherEntry(GrWeathers.snowstorm) {{
                always = true;
            }});
        } else {
            rules.weather.add(new Weather.WeatherEntry(GrWeathers.snowstorm));
        }
    }

    @Override
    public void genTile(Vec3 position, TileGen tile){
        tile.floor = getBlock(position);

        if(tile.floor == GrBlocks.purpleStone && rand.chance(0.003)){
            tile.floor = GrBlocks.purpleStoneCrater;
        }

        tile.block = tile.floor.asFloor().wall;

        if(Ridged.noise3d(seed + 1, position.x, position.y, position.z, 2, airScl) > airThresh){
            tile.block = Blocks.air;
        }

        if(Ridged.noise3d(seed + 2, position.x, position.y + 4f, position.z, 3, 6f) > 0.6){
            tile.floor = GrBlocks.galena;
        }
    }

    @Override
    protected void generate(){
        float temperature = getTemperature(sector.tile.v);

        cells(4);

//        pass((x, y) -> {
//            if(floor == Blocks.regolith && noise(x, y, 3, 0.4f, 13f, 1f) > 0.59f){
//                block = Blocks.regolithWall;
//            }
//        });

        float length = width/3f;
        Vec2 trns = Tmp.v1.trns(rand.random(360f), length);
        int
        spawnX = (int)(trns.x + width/2f), spawnY = (int)(trns.y + height/2f),
        endX = (int)(-trns.x + width/2f), endY = (int)(-trns.y + height/2f);
        float maxd = Mathf.dst(width/2f, height/2f);

        erase(spawnX, spawnY, 15);
        brush(pathfind(spawnX, spawnY, endX, endY, tile -> (tile.solid() ? 300f : 0f) + maxd - tile.dst(width/2f, height/2f)/10f, Astar.manhattan), 9);
        erase(endX, endY, 15);

        pass((x, y) -> { // cryogen
            if(floor != Blocks.shale) return;

            if(Math.abs(noise(x, y + 500f, 5, 0.6f, 40f, 1f) - 0.5f) < 0.09f){
                floor = GrBlocks.cryogenFloor;
            }

            if(nearWall(x, y)) return;

            float noise = noise(x + 300, y - x*1.6f + 100, 4, 0.8f, liqScl, 1f);

            if(noise > liqThresh){
                floor = Blocks.cryofluid;
            }
        });

        median(2, 0.6, Blocks.cryofluid);

        blend(Blocks.cryofluid, GrBlocks.cryogenFloor, 4);

        distort(10f, 12f);
        distort(5f, 7f);

        pass((x, y) -> {
            // TODO: floor alternates
            /*
            //rough rhyolite
            if(noise(x, y + 600 + x, 5, 0.86f, 60f, 1f) < 0.41f && floor == Blocks.rhyolite){
                floor = Blocks.roughRhyolite;
            }

            if(floor == Blocks.slag && Mathf.within(x, y, spawnX, spawnY, 30f + noise(x, y, 2, 0.8f, 9f, 15f))){
                floor = Blocks.yellowStonePlates;
            }

            if((floor == Blocks.arkyciteFloor || floor == Blocks.arkyicStone) && block.isStatic()){
                block = Blocks.arkyicWall;
            }*/

            float max = 0;
            for(Point2 p : Geometry.d8){
                max = Math.max(max, world.getDarkness(x + p.x, y + p.y));
            }

            if(max > 0){
                block = floor.asFloor().wall;
                if(block == Blocks.air) block = GrBlocks.corundumWall;
            }

            /*
            if(floor == Blocks.yellowStonePlates && noise(x + 78 + y, y, 3, 0.8f, 6f, 1f) > 0.44f){
                floor = Blocks.yellowStone;
            }

            if(floor == Blocks.redStone && noise(x + 78 - y, y, 4, 0.73f, 19f, 1f) > 0.63f){
                floor = Blocks.denseRedStone;
            }*/
        });

        inverseFloodFill(tiles.getn(spawnX, spawnY));

        // blend(Blocks.redStoneWall, Blocks.denseRedStone, 4);

        // make sure enemies have room
        erase(endX, endY, 6);


        tiles.getn(endX, endY).setOverlay(Blocks.spawn);

        pass((x, y) -> { // ores
            // TODO: bug, sometime ores generate not near air
            if(block != Blocks.air){
                if(nearAir(x, y)){
                    if(block == GrBlocks.galenaWall && noise(x + 78, y, 4, 0.7f, 33f, 1f) > 0.52f){
                        ore = GrBlocks.wallOreLead;
                    }else if(block != GrBlocks.galenaWall && noise(x + 782, y, 4, 0.8f, 38f, 1f) > 0.665f){
                        ore = GrBlocks.wallOreCobalt;
                    }

                }
            }else if(!nearWall(x, y)){
                if(noise(x + 999, y + 600 - x, 4, 0.63f, 45f, 1f) < 0.27f && (floor == Blocks.shale || floor == GrBlocks.cryogenFloor)){
                    ore = Blocks.oreTitanium; // replace with other titanium
                }
            }

            if(block == GrBlocks.corundumWall && rand.chance(0.11) && nearAir(x, y) && !near(x, y, 4, GrBlocks.corundumCluster)){
                block = GrBlocks.corundumCluster;
                ore = Blocks.air;
            }
        });

        pass((x, y) -> { // remove large props near resources
            if(ore.asFloor().wallOre || block.itemDrop != null || (block == Blocks.air && ore != Blocks.air)){
                removeWall(x, y, 3, b -> b instanceof TallBlock);
            }
        });

        trimDark();

        int minReservoir = rand.random(4, 7);
        int reservoirCount = 0;

        int iterations = 0;
        int maxIterations = 5;

        Seq<Point2> placed = new Seq<>(16);
        final float safeRange = 40f;
        while(reservoirCount < minReservoir && iterations++ < maxIterations){
            outer:
            for(Tile tile : tiles){
                if(rand.chance(0.00008 * (1 + iterations)) && !Mathf.within(tile.x, tile.y, spawnX, spawnY, 8f)){
                    for(Point2 otherFissure : placed){
                        //prevent spawning a fissure near the other
                        if(Mathf.within(tile.x, tile.y, otherFissure.x, otherFissure.y, safeRange)){
                            continue outer;
                        }
                    }

                    Block floor = tile.floor();

                    Floor[] blocks = reservoirBlocks(floor);
                    if(blocks == null) continue;

                    Floor fissure = blocks[0], well = blocks[1];

                    //1. select valid place for fissure placement
                    if(!validPlace(tile.x, tile.y, floor, FissureBlock.offsets)) continue;

                    //2. try to place 2..4 wells within 5..20 blocks
                    int wellCount = rand.random(3, 5);
                    int[][] spots = new int[wellCount][2];
                    boolean ok = true;

                    for(int i = 0; i < wellCount && ok; i++){
                        boolean found = false;
                        for(int attempt = 0; attempt < 8 && !found; attempt++){
                            float ang = rand.random(360f), dist = rand.random(5f, 20f);
                            int wx = tile.x + (int)(Mathf.cosDeg(ang) * dist), wy = tile.y + (int)(Mathf.sinDeg(ang) * dist);

                            if(validPlace(wx, wy, floor, WellBlock.offsets)){
                                found = true;
                                for(int j = 0; j < i; j++){
                                    if(Mathf.within(spots[j][0], spots[j][1], wx, wy, 3f)){
                                        found = false;
                                        break;
                                    }
                                }
                                if(found){
                                    spots[i][0] = wx;
                                    spots[i][1] = wy;
                                }
                            }
                        }
                        //cancel placement if not enough space for wells
                        if(!found) ok = false;
                    }

                    if(!ok) continue;

                    //3. otherwise place and continue
                    Liquid reservoirType = getReservoirLiquid(tile, reservoirCount);
                    int reservoirId = ReservoirSystem.addReservoir(reservoirType);
                    for(Point2 p : FissureBlock.offsets){
                        Tile t = tiles.getn(tile.x + p.x, tile.y + p.y);
                        t.setFloor(fissure);
                        t.extraData = reservoirId;
                    }
                    placed.add(new Point2(tile.x, tile.y));

                    for(int[] spot : spots){
                        for(Point2 p : WellBlock.offsets){
                            Tile t = tiles.getn(spot[0] + p.x, spot[1] + p.y);
                            t.setFloor(well);
                            t.extraData = reservoirId;
                        }
                    }
                    reservoirCount++;
                }
            }
        }

        for(Tile tile : tiles){
            if(tile.overlay().needsSurface && !tile.floor().hasSurface()){
                tile.setOverlay(Blocks.air);
            }
        }

        decoration(0.017f);

        Schematics.placeLaunchLoadout(spawnX, spawnY);
    }

    @Nullable Floor[] reservoirBlocks(Block floor){
        if(floor == GrBlocks.corundum){
            return new Floor[]{GrBlocks.corundumFissure, GrBlocks.corundumWell};
        }
        if(floor == GrBlocks.galena){
            return new Floor[]{GrBlocks.galenaFissure, GrBlocks.galenaWell};
        }
        if(floor == GrBlocks.purpleStone || floor == GrBlocks.alunite){ //TODO: improve drawMain in reservoir block for better blending
            return new Floor[]{GrBlocks.purpleStoneFissure, GrBlocks.purpleStoneWell};
        }
        if(floor == Blocks.shale){
            return new Floor[]{GrBlocks.shaleFissure, GrBlocks.shaleWell};
        }
        if(floor == GrBlocks.cryogenFloor){
            return new Floor[]{GrBlocks.cryogenFissure, GrBlocks.cryogenWell};
        }

        return null;
    }

    boolean validPlace(int cx, int cy, Block floor, Point2[] offsets){
        for(Point2 p : offsets){
            Tile other = tiles.get(cx + p.x, cy + p.y);
            if(other == null || other.block().solid || !sameFloor(other.floor(), floor) || nearWall(cx + p.x, cy + p.y)) return false;
        }

        return true;
    }

    boolean sameFloor(Block floor1, Block floor2){
        if(floor1 == GrBlocks.alunite || floor1 == GrBlocks.purpleStone){
            return floor2 == GrBlocks.alunite || floor2 == GrBlocks.purpleStone;
        }

        return floor1 == floor2;
    }

    Liquid getReservoirLiquid(Tile tile, int index){
        if(index < reservoirDrop.length){
            return reservoirDrop[index];
        }

        return reservoirDrop[Mathf.randomSeed(tile.pos() + seed, 0, reservoirDrop.length - 1)];
    }
}