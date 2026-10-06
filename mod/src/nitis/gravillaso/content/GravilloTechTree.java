package nitis.gravillaso.content;

import arc.struct.*;
import mindustry.type.*;
import nitis.gravillaso.core.*;
import nitis.gravillaso.core.GrObjectives.*;

import static mindustry.content.Items.*;
import static mindustry.content.Liquids.*;
import static mindustry.content.TechTree.*;
import static nitis.gravillaso.content.GrBlocks.*;
import static nitis.gravillaso.content.GrItems.*;
import static nitis.gravillaso.content.GrLiquids.*;
import static nitis.gravillaso.content.GrUnitTypes.*;

public class GravilloTechTree{
    public static void load() {
        // planned resources:
        // T1: cobalt, lead, bauxite, silicon - 1st & 2nd map
        // T2: titanium - 3rd/4th map
        // T3: oxide - 5th map
        // T4: tungsten - 6th map
        // T5: aluminium

        // T6: some kind of replacement for surge-alloy and phase-fabric
        //
        //   oxygen?
        //   hydrogen?
        //   sulfuric acid?
        //
        // silicon is acquired from bauxite
        // aluminium & oxygen? is acquired from bauxite + brine

        var costMultipliers = new ObjectFloatMap<Item>();
        /*
        costMultipliers.put(Items.oxide, 0.5f);
        costMultipliers.put(Items.surgeAlloy, 0.7f);
        costMultipliers.put(Items.carbide, 0.3f);
        costMultipliers.put(Items.phaseFabric, 0.2f);
        */

        GrPlanets.gravillo.techTree = nodeRoot("gravillo", coreBase, true, () -> {
            context().researchCostMultipliers = costMultipliers;

            // distribution
            node(cobaltConveyor, () -> {
                // TODO: junction
                node(cobaltRouter, () -> {

                });
                node(maglevConveyor, () -> {

                });
            });

            // production
            node(bauxiteCrusher, () -> {
                node(wellCollector, () -> {
                    node(pressureBooster, () -> {

                    });
                });
            });

            // power
            node(powerSection, () -> {
                // power-producers
                node(windTurbine, () -> {

                });
                node(booster, () -> {
                    node(boostRedirector, () -> {
                        node(largeBoostRedirector);
                        node(boostRouter, () -> {
                            node(largeBoostRouter);
                        });
                    });
                });
            });

            // cores
            node(draugDrone, ItemStack.with(cobalt, 1500, lead, 1000), () -> {
//            node(coreTier2, () -> {
//                node(coreTier3, () -> {
//
//                });
//            });
            });

            // turrets
            node(sight, Seq.with(new Blocked()), () -> {
                node(cobaltWall, () -> {
                   node(cobaltWallLarge, () -> {

                   });
                });
            });

            // units
            // 4-leg walkers units
            // hover-ground units
            // air-bee like support? units
            node(walkerFactory, () -> {
                node(offense);
            });

            // sectors
//            node(negativeOnCelsius, () -> {
//
//            });

            // items
            // THE ORDER WILL CHANGE 100%
            nodeProduce(cobalt, () -> {
                nodeProduce(lead, () -> {
                    nodeProduce(brine, () -> {
                        nodeProduce(aluminium, () -> {

                        });
                    });
                });

                nodeProduce(bauxite, () -> {
                    nodeProduce(silicon, () -> {

                    });

                    nodeProduce(oxygen, () -> {
                        nodeProduce(oxide, () -> {

                        });
                    });
                });

                nodeProduce(oil, () -> {

                });

                nodeProduce(cryofluid, () -> { // Use rockets for phaseFabric, (maybe replace with phaseFluid/phaseAlloy
                    nodeProduce(phaseFabric, () -> {

                    });
                });
            });
        });
    }
}