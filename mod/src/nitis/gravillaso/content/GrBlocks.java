package nitis.gravillaso.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.units.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import nitis.gravillaso.graphics.*;
import nitis.gravillaso.world.blocks.defense.*;
import nitis.gravillaso.world.blocks.distribution.*;
import nitis.gravillaso.world.blocks.environment.*;
import nitis.gravillaso.world.blocks.liquid.*;
import nitis.gravillaso.world.blocks.logic.*;
import nitis.gravillaso.world.blocks.power.*;
import nitis.gravillaso.world.blocks.production.*;
import nitis.gravillaso.world.blocks.storage.*;
import nitis.gravillaso.world.draw.*;

import static mindustry.content.Items.*;
import static mindustry.type.ItemStack.*;
import static nitis.gravillaso.content.GrItems.*;
import static nitis.gravillaso.content.GrLiquids.*;

public class GrBlocks{
    // environment
    public static Block
    corundum, corundumWall,
    galena, galenaWall,
    cryogenFloor, cryogenWall,
    alunite, aluniteWall,
    purpleStone, purpleStoneCrater, purpleStoneWall,
    moss;
    // wells and fissures
    public static Floor corundumWell, galenaWell, purpleStoneWell, shaleWell, cryogenWell;
    public static Floor corundumFissure, galenaFissure, purpleStoneFissure, shaleFissure, cryogenFissure;
    // props
    public static Block corundumBoulder, corundumCluster, galenaBoulder, purpleStoneBoulder, cryogenBoulder, mossFlower;
    // ores
    // wall ores
    public static Block wallOreLead, wallOreCobalt;
    // crafting
    public static Block siliconFurnace, aluminiumFurnace;
    // sandbox
    // walls
    public static Block cobaltWall, cobaltWallLarge;
    // defense
    public static Block booster, boostRedirector, largeBoostRedirector, boostRouter, largeBoostRouter;
    // transport
    public static Block distributionLine, maglevConveyor;
    // liquid
    public static Block screenConduit, radiantConduit, screenLiquidRouter;
    // power
    public static Block powerSection, windTurbine;
    // production
    public static Block bauxiteCrusher;
    // production - wells
    public static Block wellCollector, pressureBooster;
    // storage
    public static Block coreBase;
    // turrets
    public static Block sight, destiny, voltum, lighter, finale;
    // units
    public static Block walkerFactory;
    // payloads
    // logic
    public static Block binaryWire;
    // campaign

    public static void load(){
        // region environment
        corundum = new Floor("corundum-floor", 3){{
            attributes.set(Attribute.sand, 3f);
        }};

        corundumWall = new StaticWall("corundum-wall"){{
            corundum.asFloor().wall = this;
            attributes.set(Attribute.sand, 3f);
        }};

        galena = new Floor("galena", 4);

        galenaWall = new StaticWall("galena-wall"){{
            galena.asFloor().wall = this;
        }};

        cryogenFloor = new Floor("cryogen-floor", 3);

        cryogenWall = new StaticWall("cryogen-wall"){{
            cryogenFloor.asFloor().wall = this;
        }};

        alunite = new Floor("alunite", 3);

        aluniteWall = new StaticWall("alunite-wall"){{
            alunite.asFloor().wall = this;
        }};

        purpleStone = new Floor("purple-stone"){{
            attributes.set(Attribute.sand, 1.25f);
        }};

        purpleStoneCrater = new Floor("purple-stone-crater", 3){{
            attributes.set(Attribute.sand, 1.25f);
            blendGroup = purpleStone;
        }};

        purpleStoneWall = new StaticWall("purple-stone-wall"){{
            purpleStone.asFloor().wall = purpleStoneCrater.asFloor().wall = this;
            attributes.set(Attribute.sand, 1.25f);
        }};

        moss = new Floor("moss", 3);
        // endregion

        // region well well well and fissures
        corundumWell = new WellBlock("corundum-well"){{
            parent = blendGroup = corundum;
        }};

        galenaWell = new WellBlock("galena-well"){{
            parent = blendGroup = galena;
        }};

        purpleStoneWell = new WellBlock("purple-stone-well"){{
            parent = blendGroup = purpleStone;
        }};

        shaleWell = new WellBlock("shale-well"){{
            parent = blendGroup = Blocks.shale;
        }};

        cryogenWell = new WellBlock("cryogen-well"){{
            parent = blendGroup = cryogenFloor;
        }};

        corundumFissure = new FissureBlock("corundum-fissure"){{
            parent = blendGroup = corundum;
            variants = 0;
        }};

        galenaFissure = new FissureBlock("galena-fissure"){{
            parent = blendGroup = galena;
            variants = 0;
        }};

        purpleStoneFissure = new FissureBlock("purple-stone-fissure"){{
            parent = blendGroup = purpleStone;
            variants = 0;
        }};

        shaleFissure = new FissureBlock("shale-fissure"){{
            parent = blendGroup = Blocks.shale;
            variants = 0;
        }};

        cryogenFissure = new FissureBlock("cryogen-fissure"){{
            parent = blendGroup = cryogenFloor;
            variants = 0;
        }};
        // endregion

        // region boulders
        corundumBoulder = new Prop("corundum-boulder"){{
            variants = 3;
            customShadow = true;
            corundum.asFloor().decoration = this;
            obstructsLight = false;
        }};
        corundumCluster = new TallBlock("corundum-cluster"){{
            variants = 3;
            clipSize = 128f;
        }};

        galenaBoulder = new Prop("galena-boulder"){{
            variants = 2;
            galena.asFloor().decoration = this;
        }};

        purpleStoneBoulder = new Prop("purple-stone-boulder"){{
            variants = 2;
            customShadow = true;
            alunite.asFloor().decoration = purpleStone.asFloor().decoration = this;
            obstructsLight = false;
        }};

        cryogenBoulder = new Prop("cryogen-boulder"){{
            variants = 2;
            cryogenFloor.asFloor().decoration = this;
        }};

        mossFlower = new TreeBlock("moss-flower"){{
            variants = 1;
            clipSize = 128f;
        }};
        // endregion

        // region ores
        wallOreLead = new OreBlock("ore-wall-lead", lead){{
            wallOre = true;
        }};

        wallOreCobalt = new OreBlock("ore-wall-cobalt", cobalt){{
            wallOre = true;
        }};
        // endregion

        // region crafting
        siliconFurnace = new GenericCrafter("silicon-furnace"){{
            requirements(Category.crafting, with(cobalt, 120));
            craftEffect = Fx.none;
            outputItem = new ItemStack(silicon, 2);
            craftTime = 60f;
            size = 3;
            hasPower = true;
            hasLiquids = false;
            itemCapacity = 20;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawArcSmelt(), new DrawDefault());
            fogRadius = 3;
            ambientSound = Sounds.loopSmelter;
            ambientSoundVolume = 0.12f;

            consumeItems(with(bauxite, 3));
            consumePower(3f);
        }};

        aluminiumFurnace = new GenericCrafter("aluminium-furnace"){{
            requirements(Category.crafting, with(cobalt, 120));
            craftEffect = Fx.none;
            outputItem = new ItemStack(aluminium, 2);
            craftTime = 60f;
            size = 3;
            hasPower = true;
            hasLiquids = false;
            itemCapacity = 20;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawDefault());
            fogRadius = 3;
            ambientSound = Sounds.loopSmelter;
            ambientSoundVolume = 0.12f;

            consumeItems(with(bauxite, 3));
            consumeLiquid(brine, 1f);
            consumePower(7f);
        }};
        // endregion

        // region walls
        final int wallHealthMultiplier = 4;
        cobaltWall = new Wall("cobalt-wall"){{
            requirements(Category.defense, with(cobalt, 6));
            health = 120 * wallHealthMultiplier;
            armor = 2f;
            envDisabled |= Env.scorching;
        }};
        cobaltWallLarge = new Wall("cobalt-wall-large"){{
            requirements(Category.defense, mult(cobaltWall.requirements, 4));
            health = 120 * wallHealthMultiplier * 4;
            size = 2;
            envDisabled |= Env.scorching;
        }};
        // endregion walls

        // region defense
        booster = new Booster("booster"){{
            requirements(Category.effect, with(cobalt, 250, silicon, 120)); // TODO: price too low

            researchCostMultiplier = 10f;

            size = 2;
            regionRotated1 = 1;

            consumePower(3f);
        }};

        boostRedirector = new BoostConductor("boost-redirector"){{
            requirements(Category.effect, with(cobalt, 100, silicon, 35));

            size = 2;
            regionRotated1 = 1;
        }};

        largeBoostRedirector = new BoostConductor("large-boost-redirector"){{
            requirements(Category.effect, with(cobalt, 300, silicon, 120));

            maxBoostThroughput = 4.5f;
            size = 3;
            regionRotated1 = 1;
        }};

        boostRouter = new BoostConductor("boost-router"){{
            requirements(Category.effect, with(cobalt, 120, silicon, 50));
            splitBoost = true;

            size = 2;
            drawer = new DrawMulti(
            new DrawDefault(),
            new DrawHeatOutput(-1, false), new DrawHeatOutput(0, false), new DrawHeatOutput(+1, false),
            new DrawOverdriveProjectorPulse(Pal.redLight)
            );
            regionRotated1 = 1;
        }};

        largeBoostRouter = new BoostConductor("large-boost-router"){{
            requirements(Category.effect, with(cobalt, 250, silicon, 120));
            splitBoost = true;

            size = 3;
            drawer = new DrawMulti(
            new DrawDefault(),
            new DrawHeatOutput(-1, false), new DrawHeatOutput(0, false), new DrawHeatOutput(+1, false),
            new DrawOverdriveProjectorPulse(Pal.redLight)
            );
            regionRotated1 = 1;
        }};
        // endregion

        // region transport
        distributionLine = new DistributionLine("distribution-line"){{
            requirements(Category.distribution, with(cobalt, 1));
            health = 120;

            speed = 2f / 60f;
            itemCapacity = 8;
        }};

        maglevConveyor = new MaglevConveyor("maglev-conveyor"){{
            requirements(Category.distribution, with(aluminium, 2, silicon, 2, phaseFabric, 1));
            health = 150;

            recharge = 1f;
            speed = 6f / Time.toSeconds;
            itemCapacity = 10;
        }};

        /* TODO: replace with aluminium router
        cobaltRouter = new GrStackRouter("cobalt-router"){{
            requirements(Category.distribution, with(cobalt, 5));
            health = maglevConveyor.health;

            speed = 15;
            itemCapacity = 15;
        }};*/
        // endregion

        // region liquid
        screenConduit = new ArmoredConduit("screen-conduit"){{
            requirements(Category.liquid, with(lead, 1));
            liquidCapacity = 60f;
            liquidPressure = 1.05f;
            health = 150;
            explosivenessScale = flammabilityScale = 16f / 60f; // holy magic anuke's numbers
        }};

        radiantConduit = new RadiantConduit("radiant-conduit"){{
            requirements(Category.liquid, with(tungsten, 2, aluminium, 1));
            liquidCapacity = 60f;
            liquidPressure = 1.05f;
            health = 300;
            explosivenessScale = flammabilityScale = 20f / 60f;
        }};

        screenLiquidRouter = new LiquidRouter("screen-liquid-router"){{
            requirements(Category.liquid, with(lead, 4));
            liquidCapacity = 120f;
            underBullets = true;
            solid = false;

            explosivenessScale = flammabilityScale = 20f / 120f;
        }};
        // endregion

        // region power
        powerSection = new SquarePowerNode("power-section"){{
            requirements(Category.power, with(cobalt, 5, lead, 20));
            consumesPower = outputsPower = true;
            size = 2;
            health = 300;
            fogRadius = 3;
            laserRange = 12;
            maxNodes = 15;

            consumePowerBuffered(7500f);
        }};
        // TODO: big a 3x3/4x4 large range power sector

        windTurbine = new WindPowerGenerator("wind-turbine"){{
            requirements(Category.power, with(cobalt, 80, lead, 100));

            size = 2;
            health = 450;
            fogRadius = 3;

            powerProduction = 13.5f;
            windStrengthForMaximumEfficiency = 6f;
            minEfficiency = 0.10f;

            drawer = new DrawMulti(
            new DrawDefault(),
            new DrawBlurSpin("-rotor", windStrengthForMaximumEfficiency * 1.5f){
                @Override
                public TextureRegion[] icons(Block block){
                    return new TextureRegion[]{};
                }
            },
            new DrawRegion("-top")
            );
        }};
        // endregion

        // region production
        bauxiteCrusher = new WallCrafter("bauxite-crusher"){{
            requirements(Category.production, with(cobalt, 30, lead, 25));
            consumePower(24 / Time.toSeconds);

            drillTime = 120f;
            size = 2;
            attribute = Attribute.sand;
            output = bauxite;
            fogRadius = 2;
            researchCost = with(cobalt, 100, lead, 80);
            ambientSound = Sounds.loopDrill;
            ambientSoundVolume = 0.04f;
        }};

        wellCollector = new WellCollector("well-collector"){{
            requirements(Category.production, with(cobalt, 1));

            liquidCapacity = 160f;

            size = 2;
            fogRadius = 2;
        }};

        pressureBooster = new PressureBooster("pressure-booster"){{
            requirements(Category.production, with(cobalt, 1));

            size = 3;
            fogRadius = 5;

            consumeLiquid(oxygen, 2f);
            liquidCapacity = 300f;
            consumePower(14.5f);

            drawer = new DrawMulti(
            new DrawRegion("-bottom"),
            new DrawDefault()
            );
        }};
        // endregion

        // region storage
        coreBase = new FactoryCoreBlock("core-base"){{
            requirements(Category.effect, with(cobalt, 1000, lead, 800));
            isFirstTier = true;
            size = 4;

            unitType = GrUnitTypes.tantalus;
            health = 3500;
            itemCapacity = 2000;
            thrusterLength = 34/4f;
            armor = 5f;

            alwaysUnlocked = true;
            incinerateNonBuildable = true;
            buildCostMultiplier = 0.7f;
            requiresCoreZone = true;

            unitCapModifier = 5;
        }};
        // endregion

        // region turrets
        sight = new ItemTurret("sight"){{
            requirements(Category.turret, with(cobalt, 100, lead, 80));

            Effect sfe = new MultiEffect(Fx.shootBigColor, Fx.colorSpark);

            ammo(
            cobalt, new BasicBulletType(7.5f, 65){{
                width = 8f;
                hitSize = 7f;
                height = 15f;
                shootEffect = sfe;
                smokeEffect = Fx.shootSmallSmoke;
                ammoMultiplier = 1;
                pierceCap = 2;
                pierce = true;
                pierceBuilding = true;
                hitColor = backColor = trailColor = GrPal.cobaltShot;
                frontColor = Color.white;
                trailWidth = 2.1f;
                trailLength = 10;
                hitEffect = despawnEffect = Fx.hitBulletColor;
                buildingDamageMultiplier = 0.3f;
            }}
            );

            shake = 1f;
            ammoPerShot = 2;
            drawer = new DrawFrostTurret(){{
                parts.add(new RegionPart("-side"){{
                    progress = PartProgress.warmup;
                    moveX = 0.6f;
                    moveRot = -15f;
                    mirror = true;
                    layerOffset = 0.001f;
                    moves.add(new PartMove(PartProgress.recoil, 0.5f, -0.5f, -8f));
                }}, new RegionPart("-barrel"){{
                    progress = PartProgress.recoil;
                    moveY = -2.5f;
                }});
            }};
            shootY = 2.3f;
            outlineColor = GrPal.outline;
            size = 2;
            reload = 90f;
            shoot.shots = 2;
            shoot.shotDelay = 20f;
            recoil = 1.5f;
            range = 150;
            shootCone = 3f;
            scaledHealth = 220;
            rotateSpeed = 3.5f;

            limitRange();
            researchCostMultiplier = 0.05f;
        }};

        voltum = new PowerTurret("voltum"){{
            requirements(Category.turret, with(cobalt, 100, lead, 80));

            shootType = new BasicBulletType(){{ // TODO: this is temporal shoot type
                shootEffect = new MultiEffect(Fx.shootTitan, new WaveEffect(){{
                    colorTo = Pal.surge;
                    sizeTo = 26f;
                    lifetime = 14f;
                    strokeFrom = 4f;
                }});
                smokeEffect = Fx.shootSmokeTitan;
                hitColor = Pal.surge;

                sprite = "large-orb";
                trailEffect = Fx.missileTrail;
                trailInterval = 3f;
                trailParam = 4f;
                pierceCap = 2;
                buildingDamageMultiplier = 0.5f;
                fragOnHit = false;
                speed = 5f;
                damage = 180f;
                lifetime = 80f;
                width = height = 16f;
                backColor = Pal.surge;
                frontColor = Color.white;
                shrinkX = shrinkY = 0f;
                trailColor = Pal.surge;
                trailLength = 12;
                trailWidth = 2.2f;
                despawnEffect = hitEffect = new ExplosionEffect(){{
                    waveColor = Pal.surge;
                    smokeColor = Color.gray;
                    sparkColor = Pal.surge;
                    waveStroke = 4f;
                    waveRad = 40f;
                }};

                despawnSound = Sounds.explosionAfflict;
                shootSound = Sounds.shootAfflict;

                fragBullet = intervalBullet = new BasicBulletType(3f, 35){{
                    width = 9f;
                    hitSize = 5f;
                    height = 15f;
                    pierceCap = 3;
                    lifetime = 28f;
                    pierceBuilding = true;
                    hitColor = backColor = trailColor = Pal.surge;
                    frontColor = Color.white;
                    trailWidth = 2.1f;
                    trailLength = 5;
                    hitEffect = despawnEffect = new WaveEffect(){{
                        colorFrom = colorTo = Pal.surge;
                        sizeTo = 4f;
                        strokeFrom = 4f;
                        lifetime = 10f;
                    }};
                    buildingDamageMultiplier = 0.3f;
                    homingPower = 0.1f;
                }};

                bulletInterval = 3f;
                intervalRandomSpread = 20f;
                intervalBullets = 2;
                intervalAngle = 180f;
                intervalSpread = 300f;

                fragBullets = 20;
                fragVelocityMin = 0.5f;
                fragVelocityMax = 1.2f;
                fragLifeMin = 0.5f;
            }};

            shake = 1f;
            ammoPerShot = 2;
            drawer = new DrawFrostTurret(){{
                hasShadow = false;

                parts.add(
                new RegionPart("-core"){{
                    progress = PartProgress.warmup;
                    moves.add(new PartMove(PartProgress.warmup, 0f, 2f, 0f));
                }},
                new RegionPart("-arm"){{
                    progress = PartProgress.warmup;
                    mirror = true;
                    under = true;
                    moves.add(new PartMove(PartProgress.warmup, 1f, -0.5f, -15f));
                }}
                );
            }};
            shootY = -2;
            outlineColor = GrPal.outline;
            size = 3;
            reload = 40f;
            recoil = 2f;
            range = 60;
            shootCone = 3f;
            scaledHealth = 180;
            rotateSpeed = 1.5f;
        }};

        /*testTurret = new PayloadAmmoTurret("test-turret"){{
            requirements(Category.turret, with(cobalt, 1));
            buildVisibility = BuildVisibility.hidden; // curse stuff
            range = 45.5f * Vars.tilesize;
            size = 3;

            ammo(
            Blocks.router, new PayloadBulletType(Blocks.router, 20f),
            GrBlocks.cobaltWallLarge, new PayloadBulletType(GrBlocks.cobaltWallLarge, 10f)
            );

            limitRange();
        }};*/
        // endregion

        // region units
        walkerFactory = new UnitFactory("walker-factory"){{
            requirements(Category.units, with(cobalt, 200, silicon, 170));
            size = 3;
            configurable = false;
            plans.add(
            new UnitPlan(GrUnitTypes.offense, Time.toSeconds * 30f, with(cobalt, 40, silicon, 45))
            );
            regionSuffix = "-frost-resistant";
            fogRadius = 3;
            consumePower(1.2f);
        }};
        // endregion

        // region logic
        // TODO[lowest-priority]: implement
        binaryWire = new BinaryWire("binary-wire"){{
            requirements(Category.logic, with(cobalt, 5, silicon, 1)); // TODO: gold?
            size = 1;
            buildVisibility = BuildVisibility.hidden;
        }};
        // endregion
    }
}