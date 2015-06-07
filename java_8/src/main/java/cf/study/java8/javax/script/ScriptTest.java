package cf.study.java8.javax.script;

import static javax.script.ScriptContext.ENGINE_SCOPE;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import junit.framework.Assert;
import misc.MiscUtils;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;

public class ScriptTest {
	@Test
	public void testInvocable() throws Exception {
		ScriptEngineManager sem = new ScriptEngineManager();

		ScriptEngine se = sem.getEngineByExtension("js");

		Object evaluated = se.eval(MiscUtils.loadResAsString(ScriptTest.class, "invocable.js"));

		System.out.println(ToStringBuilder.reflectionToString(evaluated, ToStringStyle.MULTI_LINE_STYLE));

		Invocable inv = (Invocable) se;

		Runnable runnable = inv.getInterface(Runnable.class);

		System.out.println(runnable);
	}

	@Test
	public void testRunnable() throws Exception {
		ScriptEngineManager sem = new ScriptEngineManager();

		ScriptEngine se = sem.getEngineByExtension("js");

		Object evaluated = se.eval(MiscUtils.loadResAsString(ScriptTest.class, "runnable.js"));

		System.out.println(ToStringBuilder.reflectionToString(evaluated, ToStringStyle.MULTI_LINE_STYLE));

		Invocable inv = (Invocable) se;

		Runnable runnable = inv.getInterface(Runnable.class);

		System.out.println(runnable);
	}

	@Test
	public void testPreformance() throws Exception {
		ScriptEngineManager sem = new ScriptEngineManager();
		ScriptEngine se = sem.getEngineByExtension("js");
		String loadResAsString = MiscUtils.loadResAsString(ScriptTest.class, "pref_sqrt.js");

		System.out.println(se.eval(loadResAsString));
	}

	@Test
	public void testCompilable() throws Exception {
		ScriptEngineManager sem = new ScriptEngineManager();

		ScriptEngine se = sem.getEngineByExtension("js");

		Assert.assertTrue(se instanceof Compilable);

		Compilable cpl = (Compilable) se;

		String loadResAsString = MiscUtils.loadResAsString(ScriptTest.class, "pref.js");
		CompiledScript compiled = cpl.compile(loadResAsString);

		StopWatch sw = new StopWatch();

		for (int t = 0; t < 5; t++) {
			System.out.println();

			sw.reset();
			sw.start();
			for (int i = 0; i < 1000; i++) {
				compiled.eval();
			}
			sw.stop();
			System.out.println("compiled: " + sw.getTime());

			sw.reset();
			sw.start();
			for (int i = 0; i < 1000; i++) {
				se.eval(loadResAsString);
			}
			sw.stop();
			System.out.println("eval: " + sw.getTime());

			sw.reset();
			sw.start();
			for (int i = 0; i < 1000; i++) {
				for (int _i = 0; _i < 1000; _i++) {
					double re = Math.sqrt(_i);
				}
			}
			sw.stop();
			System.out.println("native: " + sw.getTime());
		}
	}

	@Test
	public void testCompiledInDifferentScriptEngine() throws Exception {
		ScriptEngineManager sem = new ScriptEngineManager();

		CompiledScript compiled = null;
		{
			ScriptEngine se = sem.getEngineByExtension("js");
			Compilable cpl = (Compilable) se;
			String loadResAsString = MiscUtils.loadResAsString(ScriptTest.class, "compiled.js");
			compiled = cpl.compile(loadResAsString);
		}

		Assert.assertNotNull(compiled);

		{
			ScriptEngine se = sem.getEngineByExtension("js");
			String loadResAsString = MiscUtils.loadResAsString(ScriptTest.class, "context.js");
			se.eval(loadResAsString);
			compiled.eval(se.getContext());
		}

		{
			ScriptEngine se = sem.getEngineByExtension("js");
			String loadResAsString = MiscUtils.loadResAsString(ScriptTest.class, "context_another.js");
			se.eval(loadResAsString);
			compiled.eval(se.getContext());
		}

		// ScriptEngine se1 = sem.getEngineByExtension("js");
		// String loadResAsString1 = MiscUtils.loadResAsString(ScriptTest.class,
		// "compiled.js");
		// CompiledScript compiled1 = cpl.compile(loadResAsString1);
		//
		// compiled1.eval(se.getContext());
	}

	@Test
	public void testContextBetweenEngines() throws Exception {
		ScriptEngineManager sem = new ScriptEngineManager();

//		{
//			ScriptContext sc = new SimpleScriptContext();
//
//			sc.getBindings(ScriptContext.GLOBAL_SCOPE);
//			ScriptEngine jse = sem.getEngineByExtension("js");
//			jse.eval("this.shared = 'abc'", sc);
//
//			ScriptEngine jse1 = sem.getEngineByExtension("js");
//			jse1.eval("print(this.shared)", jse.getContext());
//		}
//
//		{
//			ScriptEngine jse = sem.getEngineByExtension("js");
//			jse.eval("this.shared = 'abc'", sem.getBindings());
//
//			ScriptEngine jse1 = sem.getEngineByExtension("js");
//			jse1.eval("print(this.shared)", sem.getBindings());
//		}
//		
//		{
//			ScriptEngine jse = sem.getEngineByExtension("js");
//			jse.eval("var shared = 'abc'");
//
//			ScriptEngine jse1 = sem.getEngineByExtension("js");
//			jse1.eval("print(this.shared)", jse.getContext());
//			
//			 ScriptEngine pse = sem.getEngineByExtension("py");
//			 pse.eval("print(shared)", jse.getContext());
//		}
		
//		{
//			ScriptContext sc = new SimpleScriptContext();
//			ScriptEngine jse = sem.getEngineByExtension("js");
//			jse.eval("this.shared = 'abc'", sc);
//			
//			sc.getBindings(ScriptContext.ENGINE_SCOPE).putAll(jse.getBindings(ScriptContext.ENGINE_SCOPE));
//			
//			ScriptEngine jse1 = sem.getEngineByExtension("js");
//			jse1.eval("print(this.shared)", sc.getBindings(ScriptContext.ENGINE_SCOPE));
//			jse1.eval("print(this.shared)", jse.getBindings(ScriptContext.ENGINE_SCOPE));
//			jse1.eval("print(this.shared)", sc);
//			
//			
////			 ScriptEngine pse = sem.getEngineByExtension("py");
////			 pse.eval("print(shared)", sc);
//		}
		
//		{
//			ScriptContext sc = new SimpleScriptContext();
//			sc.setBindings(new SimpleBindings(), ScriptContext.GLOBAL_SCOPE);
//			
//			Bindings gb = sc.getBindings(ScriptContext.GLOBAL_SCOPE);
//			gb.put("bound", 123);
//			
//			ScriptEngine jse = sem.getEngineByExtension("js");
//			jse.eval("shared = 'abc'", sc);
//			
////			bd.putAll(jse.getBindings(ScriptContext.ENGINE_SCOPE));
//			Bindings jseEngineBindings = jse.getBindings(ScriptContext.ENGINE_SCOPE);
//			System.out.println(ToStringBuilder.reflectionToString(jseEngineBindings));
//			System.out.println(jseEngineBindings.keySet());
//			System.out.println(jseEngineBindings.values());
//			gb.putAll(jseEngineBindings);
//			
//			ScriptEngine jse1 = sem.getEngineByExtension("js");
//			jse1.eval("print(this.shared); print(bound);", sc);
//			
//			
////			 ScriptEngine pse = sem.getEngineByExtension("py");
////			 pse.eval("print(shared)", sc);
//		}
		

	}
	
	@Test
	public void testSharedBindingsBetweenEngines() throws Exception {
		ScriptEngineManager sem = new ScriptEngineManager();
		
		if (false) {
			ScriptEngine jse = sem.getEngineByExtension("js"), jse1 = sem.getEngineByExtension("js");

			Bindings bindings = new SimpleBindings();

			bindings.put("bound", 123);

			jse.eval("shared = 'abc';", bindings);

			bindings.putAll(jse.getBindings(ENGINE_SCOPE));

			jse1.eval("print(shared); print(bound);", bindings);
		}
		
		{
			ScriptEngine jse = sem.getEngineByExtension("js"), jse1 = sem.getEngineByExtension("js");

			Bindings bindings = new SimpleBindings();

			bindings.put("bound", 123);

			jse.eval("shared = 'abc';", bindings);

//			bindings.putAll(jse.getBindings(ENGINE_SCOPE));
			
//			jse1.setBindings(jse.getBindings(ENGINE_SCOPE), ENGINE_SCOPE);

			jse1.eval("print(shared); print(bound);", jse.getContext());
		}
	}
	
	@Test
	public void example() throws Exception {
		// Create a ScriptEngineManager that discovers all script engine
		// factories (and their associated script engines) that are visible to
		// the current thread's classloader.

		ScriptEngineManager manager = new ScriptEngineManager();

		// Obtain a ScriptEngine that supports the JavaScript short name.
		ScriptEngine engine = manager.getEngineByName("javascript");

		// Initialize the color and shape script variables.
		engine.put("color", "red");
		engine.put("shape", "rectangle");

		// Evaluate a script that outputs the values of these variables.
		engine.eval("print(color); print(shape);");

		// Save the current bindings object.
		Bindings oldBindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);

		// Replace the bindings with a new bindings that overrides color and shape.
		Bindings newBindings = engine.createBindings();
		newBindings.put("color", "blue");
		engine.setBindings(newBindings, ScriptContext.ENGINE_SCOPE);
		engine.put("shape", "triangle");

		// Evaluate the script.
		engine.eval("print(color); print(shape);");

		// Restore the original bindings.
		engine.setBindings(oldBindings, ScriptContext.ENGINE_SCOPE);

		// Evaluate the script.
		engine.eval("print(color); print(shape);");
	}
}