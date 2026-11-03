package com.duo;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import org.springframework.data.repository.Repository;
import org.springframework.web.bind.annotation.RestController;

/**
 * Guarda as fronteiras do monólito modular (DEC-020, seção 9 do CLAUDE.md).
 * Convenção: a API pública de um módulo fica na raiz do pacote ({@code com.duo.<modulo>});
 * a implementação fica em {@code com.duo.<modulo>.internal}.
 */
@AnalyzeClasses(packages = "com.duo", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

	// Os módulos ainda têm poucas classes; "vazio" é válido até o épico 1 preenchê-los.
	@ArchTest
	static final ArchRule modulesHaveNoCycles = slices()
		.matching("com.duo.(*)..")
		.should().beFreeOfCycles()
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule controllersDoNotTouchRepositories = noClasses()
		.that().areAnnotatedWith(RestController.class)
		.should().dependOnClassesThat().areAssignableTo(Repository.class)
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule internalsAreNotUsedByOtherModules = classes()
		.that().resideInAPackage("com.duo.*.internal..")
		.should(onlyBeUsedByTheirOwnModule())
		.allowEmptyShould(true);

	private static ArchCondition<JavaClass> onlyBeUsedByTheirOwnModule() {
		return new ArchCondition<>("only be used by classes of their own module") {
			@Override
			public void check(JavaClass target, ConditionEvents events) {
				for (Dependency dependency : target.getDirectDependenciesToSelf()) {
					JavaClass origin = dependency.getOriginClass();
					if (!moduleOf(origin).equals(moduleOf(target))) {
						events.add(SimpleConditionEvent.violated(dependency, dependency.getDescription()));
					}
				}
			}
		};
	}

	/** "com.duo.auth.internal.Foo" -> "auth"; classes fora de um módulo (ex.: com.duo.X) -> "". */
	private static String moduleOf(JavaClass javaClass) {
		String[] parts = javaClass.getPackageName().split("\\.");
		return parts.length > 2 ? parts[2] : "";
	}
}
