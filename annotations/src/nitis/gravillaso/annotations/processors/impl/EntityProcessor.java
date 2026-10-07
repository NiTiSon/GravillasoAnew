package nitis.gravillaso.annotations.processors.impl;

import arc.util.*;
import arc.util.io.*;
import arc.struct.*;
import com.squareup.javapoet.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.world.*;
import nitis.gravillaso.annotations.Annotations.*;
import nitis.gravillaso.annotations.processors.*;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.util.*;

/**
 * Whenever a field is annotated with {@link EntityDef @EntityDef} (e.g. a unit content field),
 * generates an entity class in the gen package extending the vanilla base entity for the declared
 * components, and registers it in {@link mindustry.gen.EntityMapping} at class load so it gets a
 * valid class ID.
 * <p>
 * Assign the generated class to the field's {@code constructor} when the content is created:
 * <pre>{@code offense = new UnitType("offense"){{ constructor = OffenseUnit::new; }};}</pre>
 */
public class EntityProcessor extends BaseProcessor{
	{
		rounds = 1;
	}

	@Override
	public Set<String> getSupportedAnnotationTypes(){
		return new HashSet<>(List.of("nitis.gravillaso.annotations.Annotations.EntityDef"));
	}

	@Override
	public void process(RoundEnvironment roundEnv) throws Exception{
		for(Element element : roundEnv.getElementsAnnotatedWith(EntityDef.class)){
			if(!(element instanceof VariableElement field)){
				continue;
			}

			Seq<String> components = componentsOf(field);
			ClassName base = baseFor(components);
			if(base == null){
				Log.err("@EntityDef on '" + field.getSimpleName() + "' has unsupported components " + components
					+ "; expected a Unitc-based def. Supported: Unitc, Legsc.");
				continue;
			}

			String name = Strings.capitalize(field.getSimpleName().toString()) + "Unit";

			TypeSpec.Builder entity = TypeSpec.classBuilder(name)
			.addModifiers(Modifier.PUBLIC)
			.superclass(base)
			.addField(FieldSpec.builder(int.class, "classId", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
				.initializer("$T.register($S, $L::new)", cName(EntityMapping.class), field.getSimpleName(), name)
				.build())
			.addMethod(MethodSpec.methodBuilder("classId")
				.addAnnotation(Override.class)
				.returns(int.class)
				.addModifiers(Modifier.PUBLIC)
				.addStatement("return classId")
				.build());

			if(components.contains(cName(BuildingTetherc.class).canonicalName())){
				emitTether(entity);
			}

			write(entity.build());
		}
	}

	private void emitTether(TypeSpec.Builder entity){
		entity.addSuperinterface(cName(BuildingTetherc.class))
		.addField(FieldSpec.builder(cName(Building.class), "building", Modifier.PUBLIC).build())
		.addMethod(MethodSpec.methodBuilder("building")
			.returns(cName(Building.class))
			.addModifiers(Modifier.PUBLIC)
			.addStatement("return building")
			.build())
		.addMethod(MethodSpec.methodBuilder("building")
			.addParameter(cName(Building.class), "building")
			.addModifiers(Modifier.PUBLIC)
			.addStatement("this.building = building")
			.build())
		.addMethod(MethodSpec.methodBuilder("update")
			.addAnnotation(Override.class)
			.addModifiers(Modifier.PUBLIC)
			.addStatement("super.update()")
			.addCode("if(building == null || !building.isValid() || building.team != team){\n    $T.unitDespawn(self());\n}\n", cName(Call.class))
			.build())
		.addMethod(MethodSpec.methodBuilder("write")
			.addAnnotation(Override.class)
			.addModifiers(Modifier.PUBLIC)
			.addParameter(cName(Writes.class), "write")
			.addStatement("super.write(write)")
			.addStatement("$T.writeBuilding(write, building)", cName(TypeIO.class))
			.build())
		.addMethod(MethodSpec.methodBuilder("read")
			.addAnnotation(Override.class)
			.addModifiers(Modifier.PUBLIC)
			.addParameter(cName(Reads.class), "read")
			.addStatement("super.read(read)")
			.addStatement("building = $T.readBuilding(read)", cName(TypeIO.class))
			.build());
	}

	/** Reads the {@code value()} Class array through mirrors; calling {@code annotation.value()} directly throws MirroredTypesException. */
	private Seq<String> componentsOf(Element element){
		Seq<String> out = new Seq<>();
		for(AnnotationMirror mirror : element.getAnnotationMirrors()){
			if(!mirror.getAnnotationType().toString().equals(EntityDef.class.getCanonicalName())){
				continue;
			}
			for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()){
				if(!entry.getKey().getSimpleName().toString().equals("value")){
					continue;
				}
				for(AnnotationValue value : (List<? extends AnnotationValue>)entry.getValue().getValue()){
					out.add((value.getValue()).toString());
				}
			}
		}
		return out;
	}

	private ClassName baseFor(Seq<String> components){
		//legs units need a Legsc base
		if(components.contains(mindustry.gen.Legsc.class.getName())){
			return cName(LegsUnit.class);
		}
		if(components.contains(mindustry.gen.Unitc.class.getName())){
			return cName(UnitEntity.class);
		}
		return null;
	}
}