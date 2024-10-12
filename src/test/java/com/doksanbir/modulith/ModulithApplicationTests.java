package com.doksanbir.modulith;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;

@SpringBootTest
class ModulithApplicationTests {

    @Test
    void contextLoads() {
    }

}
