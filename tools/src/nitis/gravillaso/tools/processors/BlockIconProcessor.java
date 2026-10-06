package nitis.gravillaso.tools.processors;

import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.TextureAtlas.AtlasRegion;
import arc.graphics.g2d.TextureRegion;
import arc.math.*;
import arc.struct.Seq;
import arc.util.Log;

import mindustry.game.Team;
import mindustry.world.Block;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.legacy.LegacyBlock;
import mindustry.world.meta.BuildVisibility;

import nitis.gravillaso.tools.*;
import nitis.gravillaso.tools.GeneratedAtlas.GeneratedRegion;
import nitis.gravillaso.world.blocks.distribution.*;

import static mindustry.Vars.*;

/**
 * Mirrors mindustry.tools.Generators#generate("block-icons"): outlines block sprites, recolors
 * '-team' stencils per palette team, and composites '-full', '-icon-logic' and '-ui' icons.
 * Ore icons stay in OreGenerator, average colors in ColorProcessor. Runs before AA/bleed.
 */
public class BlockIconProcessor implements SpriteProcessor{
    static final int maxUiIcon = 128;

    @Override
    public void process(){
        content.blocks().each(b -> b.minfo != null && b.minfo.mod == Tools.mod
            && !b.isAir() && !(b instanceof ConstructBlock) && !(b instanceof OreBlock) && !(b instanceof LegacyBlock), block -> {
            try{
                block.init();
                block.load();
                block.loadIcon();
                process(block);
            }catch(Exception e){
                Log.err("Failed to generate icons for @", block);
                Log.err(e);
            }
        });
    }

    private void process(Block block){

        Seq<TextureRegion> toOutline = new Seq<>();
        block.getRegionsToOutline(toOutline);

        TextureRegion[] regions = block.getGeneratedIcons();
        if(regions.length == 0 || !regions[0].found()){
            Log.info("Skipped block @, no regions", block);
            return;
        }

        for(TextureRegion region : toOutline){
            Pixmap pix = get(region);
            if(pix != null){
                save(pix.outline(block.outlineColor, block.outlineRadius), outlineName(region) + "-outline", SpriteProcessor.outlineFile(outlineName(region) + "-outline"));
            }
        }

        for(TextureRegion region : block.makeIconRegions()){
            Pixmap pix = get(region);
            if(pix != null){
                save(pix.outline(block.outlineColor, block.outlineRadius), outlineName(region) + "-outline", SpriteProcessor.outlineFile(outlineName(region) + "-outline"));
            }
        }

        Pixmap shardTeamTop = null;

        if(block.teamRegion.found()){
            Pixmap teamr = get(block.teamRegion);

            for(Team team : Team.all){
                if(team.hasPalette){
                    Pixmap out = new Pixmap(teamr.width, teamr.height);
                    teamr.each((x, y) -> {
                        int color = teamr.getRaw(x, y);
                        int index = color == 0xffffffff ? 0 : color == 0xdcc6c6ff ? 1 : color == 0x9d7f7fff ? 2 : -1;
                        out.setRaw(x, y, index == -1 ? teamr.getRaw(x, y) : team.palettei[index]);
                    });

                    String regionName = block.name.startsWith("gr-") ? block.name.substring("gr-".length()) : block.name;
                    regionName += "-team-" + team.name;
                    save(out, regionName, SpriteProcessor.teamFile(regionName));

                    if(team == Team.sharded){
                        shardTeamTop = out;
                    }
                }
            }
        }

        Pixmap last = null;
        if(block.outlineIcon){
            TextureRegion region = regions[block.outlinedIcon >= 0 ? block.outlinedIcon : regions.length - 1];
            Pixmap base = get(region);
            if(base == null) return;
            Pixmap out = last = base.outline(block.outlineColor, block.outlineRadius);

            if(block.outlinedIcon >= 0){
                for(int i = block.outlinedIcon + 1; i < regions.length; i++){
                    Pixmap layer = get(regions[i]);
                    if(layer != null) out.draw(layer, true);
                }
            }

            //the outlined version replaces the source sprite
            save(out, name(region));
        }

        Pixmap image = get(regions[0]).copy();

        int i = 0;
        for(TextureRegion region : regions){
            i++;
            Pixmap layer;
            if(i != regions.length || last == null){
                layer = get(region);
            }else{
                layer = last;
            }
            if(i > 1 && layer != null) image.draw(layer, true);

        }

        //draw shard (default team top) on top of the base sprite;
        //vanilla blocks include the team region in getGeneratedIcons(), modded ones don't
        if(shardTeamTop != null){
            image.draw(shardTeamTop, true);
        }

        if(!(regions.length == 1 && name(regions[0]).equals(block.name) && shardTeamTop == null)){
            save(image, "block-" + block.name + "-full", SpriteProcessor.fullFile("block-" + block.name + "-full"));
        }

        if(block.buildVisibility != BuildVisibility.hidden){
            saveScaled(image, block.name + "-icon-logic", Math.min(32 * 3, image.width), SpriteProcessor.uiFile(block.name + "-icon-logic"));
        }

        saveScaled(image, "block-" + block.name + "-ui", maxUiIcon, SpriteProcessor.uiFile("block-" + block.name + "-ui"));
        if(block instanceof MaglevConveyor maglevBlock){
            image = get(maglevBlock.phaseIcon()).copy();
            saveScaled(image, "block-" + block.name + "-phase" + "-ui", maxUiIcon, SpriteProcessor.uiFile("block-" + block.name + "-phase" + "-ui"));
        }
    }

    /** Returns the backing pixmap of an atlas-generated region, or null when missing. */
    private static Pixmap get(TextureRegion region){
        return region instanceof GeneratedRegion gen && gen.found() ? gen.pixmap() : null;
    }

    private static String name(TextureRegion region){
        return ((AtlasRegion)region).name;
    }

    private static String outlineName(TextureRegion region){
        String name = ((AtlasRegion)region).name;

        if (name.startsWith("gr-")) name = name.substring(3);
        return name;
    }

    private static void save(Pixmap pixmap, String name){
        save(pixmap, name, null);
    }

    private static void save(Pixmap pixmap, String name, Fi file){
        //reuse the existing file when the region was already written by another processor
        GeneratedRegion existing = Tools.atlas.find(name);
        if(file == null) file = existing.file != null ? existing.file : Fi.get("sprites").child(name + ".png");
        new GeneratedRegion(name, pixmap, file).save(true);
    }

    private static void saveScaled(Pixmap pixmap, String name, int maxSize, Fi file){
        if(pixmap.width > maxSize){
            save(Pixmaps.scale(pixmap, maxSize, Mathf.round((float)maxSize / pixmap.width * pixmap.height), true), name, file);
        }else{
            save(pixmap, name, file);
        }
    }
}
