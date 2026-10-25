package com.neolama.common.exception.support;

import com.neolama.common.exception.TestApplication;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

/**
 * Boots {@link TestApplication} with MockMvc and the shared message bundles: {@code
 * i18n/test-messages} for test entries, then the library's own {@code i18n/problems}.
 *
 * <p>Tests that need other bundles or properties add {@code @TestPropertySource} on a nested class.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = TestApplication.class,
    properties = "spring.messages.basename=" + AdviceTest.BASENAMES)
@AutoConfigureMockMvc
public @interface AdviceTest {

  /** Message bundles used by advice tests. */
  String BASENAMES = "i18n/test-messages,i18n/problems";
}
