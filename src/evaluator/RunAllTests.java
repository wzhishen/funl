package evaluator;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * JUnit test suite for project Funl
 * @author Zhishen Wen
 * @version Mar 24, 2013
 */
@RunWith(Suite.class)
@SuiteClasses({ 
    FunlTest.class, 
    ParserTest.class, 
    ParserTestDave.class,
    TreeTest.class })
public class RunAllTests { }
