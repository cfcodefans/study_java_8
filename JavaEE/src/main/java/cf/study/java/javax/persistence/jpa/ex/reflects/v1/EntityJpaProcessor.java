package cf.study.java.javax.persistence.jpa.ex.reflects.v1;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.hibernate.internal.SessionImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cf.study.java.javax.cdi.weld.WeldTest;
import cf.study.java.javax.persistence.dao.JpaModule;
import cf.study.java.javax.persistence.jpa.ex.reflects.v1.entity.BaseEn;
import cf.study.java.javax.persistence.jpa.ex.reflects.v1.entity.ClassEn;
import cf.study.java.javax.persistence.jpa.ex.reflects.v1.entity.FieldEn;
import cf.study.java.javax.persistence.jpa.ex.reflects.v1.entity.JarEn;
import cf.study.java.javax.persistence.jpa.ex.reflects.v1.entity.MethodEn;
import cf.study.java.javax.persistence.jpa.ex.reflects.v1.entity.PackageEn;
import cf.study.java.javax.persistence.jpa.ex.reflects.v1.entity.ParameterEn;
import cf.study.java.javax.persistence.jpa.ex.reflects.v1.entity.SourceEn;
import cf.study.java.lang.reflect.Reflects;

public class EntityJpaProcessor {
	@BeforeClass
	public static void setUp() {
		WeldTest.setUp();
		JpaModule.instance();
	}

	public Long cursor = 0l;

	public EntityJpaProcessor preload(EntityJpaProcessor base, Class<?>... clzz) {
		if (ArrayUtils.isEmpty(clzz))
			return this;

		this.base = base;
		Stream.of(clzz).forEach(this::processClass);

		{
			ReflectDao dao = ReflectDao.threadLocal.get();
			cursor = (Long) dao.findOneEntity("select be.id from BaseEn be order by be.id desc");
			cursor = ObjectUtils.defaultIfNull(cursor, 0l);
		}

		Map<String, AtomicReference<ClassEn>> _classEnPool = MapUtils
				.synchronizedMap(new LinkedHashMap<String, AtomicReference<ClassEn>>(21000));

		roots.parallelStream().forEach((be) -> {
			ReflectDao dao = ReflectDao.threadLocal.get();
			dao.beginTransaction();
			traverse(be, (_be) -> {
				if (_be instanceof ClassEn) {
					ClassEn ce = (ClassEn) _be;
					ce.source = srcEnPool.get(ce.name);
					_classEnPool.put(ce.name, new AtomicReference<ClassEn>(ce));
					dao.getEm().flush();
				}
				dao.create(_be);
			} , () -> {} );

			dao.endTransaction();
		} );

		{
			// ReflectDao dao = ReflectDao.threadLocal.get();
			// classEnPool.values().stream().filter(ce->(ce.id ==
			// null)).forEach(ce->{
			// ce.id = (Long)dao.findOneEntity("select ce.id from ClassEn ce
			// where ce.name=?1 and ce.category=?2", ce.name, ce.category);
			// });
			this.classEnPool.clear();
			this.classEnPool.putAll(_classEnPool);
		}

		return this;
	}

	public EntityJpaProcessor process() {
		ConcurrentLinkedQueue<String> sqlQueue = new ConcurrentLinkedQueue<String>();

		classEnPool.values().stream().map(AtomicReference::get).forEach(ce -> {
			traverse(ce, (_be) -> {
				sqlQueue.addAll(associateByNativeSql(_be));
			} , () -> {
			} );
		} );

		System.out.println(String.format("size of sql: %d", sqlQueue.size()));
		ReflectDao dao = ReflectDao.threadLocal.get();
		dao.beginTransaction();
		Connection conn = dao.getEm().unwrap(SessionImpl.class).connection();
		sqlQueue.stream().parallel().forEach(sql -> {
			System.out.println(sql);
			try {
				conn.createStatement().execute(sql);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} );

		dao.endTransaction();

		return this;
	}

	@SuppressWarnings("unchecked")
	public EntityJpaProcessor reorganize() {
		ReflectDao dao = ReflectDao.threadLocal.get();
		List<BaseEn> beList = dao.queryEntity(
				"select distinct be from BaseEn be left join fetch be.children kids where be.id > ?1", cursor);
		System.out.println("loaded BaseEn: " + beList.size());

		packageEnPool.clear();
		classEnPool.clear();

		beList.forEach(be -> {
			if (be instanceof PackageEn) {
				PackageEn pe = (PackageEn) be;
				packageEnPool.put(pe.name, pe);
			} else if (be instanceof ClassEn) {
				ClassEn ce = (ClassEn) be;
				classEnPool.put(ce.name, new AtomicReference<ClassEn>(ce));
			}
		} );

		classEnPool.values().stream().map(AtomicReference::get).forEach(ce -> {
			ce.loadClass();
			reprocessClassEn(ce.clazz);
		} );

		return this;
	}

	@Test
	public void test1() {
		EntityJpaProcessor ep = assembler();
		ep.clazzProc = (clz) -> {
			System.out.println(clz);
			ep.processClass(clz);
			return null;
		};
		ep.processClass(Object.class);
		System.out.println("process class: " + ep.classEnPool.size());
	}

	@Test
	public void test2() {
		EntityJpaProcessor ep = assembler();
		ep.preload(null, Object.class);
	}

	@Test
	public void test3() {
		EntityJpaProcessor ep = assembler();
		ep.preload(null, Object.class).process();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test4() {
		String libName = "junit";
		List<Class<?>> clazz = Reflects.extractClazz(Reflects.getJarFileInClassPath(libName));

		System.out.println(String.format("has loaded %d classes from %s", clazz.size(), libName));

		EntityJpaProcessor ep = assembler();

		EntityJpaProcessor base = assembler();
		{
			ReflectDao dao = ReflectDao.threadLocal.get();
			List<ClassEn> ceList = (List<ClassEn>) dao.queryEntity("select ce from ClassEn ce");
			ceList.forEach(ce -> base.classEnPool.put(ce.name, new AtomicReference<ClassEn>(ce)));

			List<PackageEn> peList = (List<PackageEn>) dao.queryEntity("select pe from PackageEn pe");
			peList.forEach(pe -> base.packageEnPool.put(pe.name, pe));
		}

		ep.preload(base, clazz.toArray(new Class<?>[0])).process();

		ep.classEnPool.forEach((name, ce) -> System.out.println(ce));
		ep.packageEnPool.forEach((name, pe) -> System.out.println(pe));
	}

	public List<String> associateByNativeSql(BaseEn be) {
		if (be == null)
			return Collections.emptyList();

		List<String> sb = new LinkedList<String>();
		if (be instanceof ClassEn) {
			ClassEn ce = (ClassEn) be;
			Class<?> superClz = ce.clazz.getSuperclass();
			if (superClz != null) {
				sb.add(String.format("update class_en set super=%d where id=%d;", loadClassEn(superClz).id, ce.id));
			}

			Stream.of(ce.clazz.getAnnotations()).forEach(anClz -> {
				ClassEn an = loadClassEn(anClz.annotationType());
				sb.add(String.format("insert into annotations (base_en_id, annotation_en_id) values (%d, %d);", be.id,
						an.id));
			} );

			{
				final ClassEn _ce = ce;
				Stream.of(ce.clazz.getInterfaces()).forEach((_inf) -> {
					ClassEn inf = loadClassEn(_inf);
					sb.add(String.format("insert into interfaces (implement_en_id, interface_en_id) values (%d,%d);",
							_ce.id, inf.id));
				} );
			}
		}

		if (be instanceof MethodEn) {
			MethodEn me = (MethodEn) be;
			if (me.method instanceof Method) {
				Method md = (Method) me.method;
				if (md.getReturnType() != null) {
					sb.add(String.format("update method_en set return_clz_id=%d where id=%d;",
							loadClassEn(md.getReturnType()).id, me.id));
				}

			}

			Stream.of(me.method.getAnnotations()).forEach(anClz -> {
				ClassEn an = loadClassEn(anClz.annotationType());
				sb.add(String.format("insert into annotations (base_en_id, annotation_en_id) values (%d, %d);", be.id,
						an.id));
			} );

			Executable exe = me.method;
			Stream.of(exe.getExceptionTypes()).forEach(exClz -> {
				sb.add(String.format("insert into exceptions (method_en_id, exception_en_id) values (%d,%d);", me.id,
						loadClassEn(exClz).id));
			} );
		}

		if (be instanceof FieldEn) {
			FieldEn fe = (FieldEn) be;
			sb.add(String.format("update field_en set field_clz_id=%d where id=%d;", loadClassEn(fe.field.getType()).id,
					fe.id));

			Stream.of(fe.field.getAnnotations()).forEach(anClz -> {
				ClassEn an = loadClassEn(anClz.annotationType());
				sb.add(String.format("insert into annotations (base_en_id, annotation_en_id) values (%d, %d);", be.id,
						an.id));
			} );
		}

		if (be instanceof ParameterEn) {
			ParameterEn pe = (ParameterEn) be;
			sb.add(String.format("update param_en set param_clz_id=%d where id=%d;",
					loadClassEn(pe.parameter.getType()).id, pe.id));

			Stream.of(pe.parameter.getAnnotations()).forEach(anClz -> {
				ClassEn an = loadClassEn(anClz.annotationType());
				sb.add(String.format("insert into annotations (base_en_id, annotation_en_id) values (%d, %d);", be.id,
						an.id));
			} );
		}

		return sb;
	}

	public static EntityJpaProcessor assembler() {
		EntityJpaProcessor ep = new EntityJpaProcessor();
		ep.clazzProc = (clz) -> {
			ep.processClass(clz);
			return null;
		};
		ep.clazzGetter = ep::getClassEn;
		return ep;
	}

	public static EntityJpaProcessor associator() {
		EntityJpaProcessor ep = new EntityJpaProcessor();
		ep.clazzProc = (clz) -> {
			ep.reprocessClassEn(clz);
			return null;
		};
		ep.clazzGetter = ep::loadClassEn;
		return ep;
	}

	public static void traverse(BaseEn be, Consumer<BaseEn> act, Runnable interAct) {
		if (be instanceof ClassEn) {
			System.out.println(be);
		}
		act.accept(be);
		be.children.forEach(en -> traverse(en, act, interAct));
		interAct.run();
	}

	public EntityJpaProcessor base;

	public final ConcurrentHashMap<String, AtomicReference<ClassEn>> classEnPool = new ConcurrentHashMap<String, AtomicReference<ClassEn>>(
			21000);
	public Function<Class<?>, ClassEn> clazzProc = null;
	public Function<Class<?>, ClassEn> clazzGetter = null;

	public BiFunction<ClassEn, Field, FieldEn> fieldProc = null;
	public final Map<String, ClassEn> inflatedClassEnPool = MapUtils
			.synchronizedMap(new LinkedHashMap<String, ClassEn>(21000));
	public final Map<String, PackageEn> packageEnPool = MapUtils
			.synchronizedMap(new LinkedHashMap<String, PackageEn>(1000));
	public final Collection<BaseEn> roots = Collections.synchronizedCollection(new LinkedHashSet<BaseEn>());

	private static final Logger log = LoggerFactory.getLogger(EntityJpaProcessor.class);

	public ClassEn getClassEnFromCache(String clzName) {
		if (StringUtils.isBlank(clzName))
			return null;

		if (base != null) {
			ClassEn ce = base.getClassEnFromCache(clzName);
			if (ce != null)
				return ce;
		}

		AtomicReference<ClassEn> ref = classEnPool.get(clzName);
		return ref == null ? null : ref.get();
	}

	public PackageEn getPackageEnFromCache(String pkgName) {
		if (StringUtils.isBlank(pkgName))
			return null;

		if (base != null) {
			PackageEn pe = base.getPackageEnFromCache(pkgName);
			if (pe != null)
				return pe;
		}

		return packageEnPool.get(pkgName);
	}

	public void processAnnotation(BaseEn be, AnnotatedElement ae) {
		if (be == null || ae == null)
			return;
		Stream.of(ae.getDeclaredAnnotations()).forEach((an) -> {
			ClassEn ce = clazzProc.apply(an.annotationType());
			if (ce != null)
				be.annotations.add(ce);
		} );
	}

	public ClassEn processClass(Class<?> clz) {
		if (clz == null)
			return null;

		while (clz.isArray()) {
			clz = clz.getComponentType();
		}

		ClassEn _ce = null;
		try {

			final PackageEn pe = processPackageEn(clz.getPackage());
			final String clzName = Reflects.checkClzName(clz);

			_ce = getClassEnFromCache(clzName);
			if (_ce != null) {
				return _ce;
			}

			if (classEnPool.putIfAbsent(clzName, new AtomicReference<ClassEn>()) == null) {
				AtomicReference<ClassEn> ref = classEnPool.get(clzName);

				_ce = new ClassEn(clz, null);// ObjectUtils.defaultIfNull(enclosingClassEn,
												// pe));
				if (ref.getAndSet(_ce) != null) {
					log.warn("found repeated: " + clzName);
				}

				Class<?> enclosingClz = ClassEn.getEnclossingClz(clz);
				ClassEn enclosingClassEn = processClass(enclosingClz);
				_ce.pkg = pe;
				_ce.enclosing = ObjectUtils.defaultIfNull(enclosingClassEn, pe);
				if (_ce.enclosing != null)
					_ce.enclosing.children.add(_ce);
			} else {
				AtomicReference<ClassEn> ref = classEnPool.get(clzName);
				while ((_ce = ref.get()) == null) {
					Thread.sleep(1);
				}
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
			return _ce;
		}

		return processClassEn(_ce);
	}

	public ClassEn processClassEn(final ClassEn ce) {
		if (ce == null)
			return ce;

		if (ce.enclosing == null) {
			ce.enclosing = ce.pkg;
		}

		if (ce.enclosing == null) {
			roots.add(ce);
		}

		Class<?> clz = ce.clazz;

		processAnnotation(ce, clz);

		ce.superClz = clazzProc.apply(clz.getSuperclass());

		Stream.of(clz.getInterfaces()).map(clazzProc).filter(infCe -> infCe != null).forEach(ce.infs::add);

		Stream.of(clz.getDeclaredFields()).forEach((fd) -> processFieldEn(ce, fd));
		Stream.of(clz.getDeclaredConstructors()).forEach((con) -> processMethodEn(ce, con));
		Stream.of(clz.getDeclaredMethods()).forEach(method -> processMethodEn(ce, method));

		return ce;
	}

	private ClassEn loadClassEn(Class<?> clz) {
		ClassEn _ce = null;
		if (clz == null)
			return _ce;

		while (clz.isArray()) {
			clz = clz.getComponentType();
		}

		try {
			String clzName = Reflects.checkClzName(clz);

			_ce = inflatedClassEnPool.get(clzName);
			if (_ce != null) {
				return _ce;
			}

			_ce = getClassEnFromCache(clzName);
			_ce.clazz = clz;
			inflatedClassEnPool.put(clzName, _ce);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return _ce;
	}

	private ClassEn getClassEn(Class<?> clz) {
		ClassEn _ce = null;
		try {
			final PackageEn pe = processPackageEn(clz.getPackage());
			final String clzName = Reflects.checkClzName(clz);

			_ce = getClassEnFromCache(clzName);
			if (_ce != null) {
				return _ce;
			}

			if (classEnPool.putIfAbsent(clzName, new AtomicReference<ClassEn>()) == null) {
				AtomicReference<ClassEn> ref = classEnPool.get(clzName);

				_ce = new ClassEn(clz, null);// ObjectUtils.defaultIfNull(enclosingClassEn,
												// pe));
				if (ref.getAndSet(_ce) != null) {
					log.warn("found repeated: " + clzName);
				}

				Class<?> enclosingClz = ClassEn.getEnclossingClz(clz);
				ClassEn enclosingClassEn = clazzProc.apply(enclosingClz);
				_ce.pkg = pe;
				_ce.enclosing = ObjectUtils.defaultIfNull(enclosingClassEn, pe);
				if (_ce.enclosing != null)
					_ce.enclosing.children.add(_ce);
			} else {
				AtomicReference<ClassEn> ref = classEnPool.get(clzName);
				while ((_ce = ref.get()) == null) {
					Thread.sleep(1);
				}
				;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return _ce;
		}
		return _ce;
	}

	public FieldEn processFieldEn(ClassEn ce, Field field) {
		if (field == null || ce == null)
			return null;

		FieldEn fe = FieldEn.instance(ce, field);
		processAnnotation(fe, field);
		fe.fieldType = clazzProc.apply(field.getType());

		return fe;
	}

	public MethodEn processMethodEn(ClassEn ce, Executable exe) {
		if (exe == null || ce == null)
			return null;

		MethodEn me = MethodEn.instance(ce, exe);

		processAnnotation(me, exe);

		if (exe instanceof Method) {
			Method method = (Method) exe;
			me.returnClass = clazzProc.apply(method.getReturnType());
		}

		Stream.of(exe.getExceptionTypes()).forEach((exClz) -> {
			ClassEn exce = clazzProc.apply(exClz);
			if (exce != null)
				me.exceptionClzz.add(exce);
		} );

		Stream.of(exe.getParameters()).forEach(param -> processParameterEn(me, param));

		return me;
	}

	public synchronized PackageEn processPackageEn(Package _package) {
		if (_package == null)
			return null;

		String pkgName = _package.getName();

		PackageEn _pe = getPackageEnFromCache(pkgName);

		if (_pe != null)
			return _pe;

		PackageEn pe = new PackageEn(_package, processPackageEn(PackageEn.getParentPkg(_package)));
		if (pe.enclosing == null) {
			roots.add(pe);
		}

		packageEnPool.put(pkgName, pe);
		processAnnotation(pe, _package);

		return pe;
	}

	public ParameterEn processParameterEn(MethodEn me, Parameter param) {
		if (param == null || me == null)
			return null;

		ParameterEn pe = ParameterEn.instance(me, param);
		processAnnotation(pe, param);
		pe.paramType = clazzProc.apply(param.getType());

		return pe;
	}

	public synchronized ClassEn reprocessClassEn(Class<?> clz) {
		if (clz == null)
			return null;

		while (clz.isArray()) {
			clz = clz.getComponentType();
		}

		ClassEn _ce = null;
		try {
			String clzName = Reflects.checkClzName(clz);

			_ce = inflatedClassEnPool.get(clzName);
			if (_ce != null) {
				return _ce;
			}

			_ce = classEnPool.get(clzName).get();
			_ce.clazz = clz;
			inflatedClassEnPool.put(clzName, _ce);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return processClassEn(_ce);
	}

	public static Map<String, SourceEn> loadSource(final File srcZip) {
		Map<String, SourceEn> reMap = new HashMap<String, SourceEn>();
		if (!(srcZip != null && srcZip.isFile() && srcZip.canRead())) {
			return reMap;
		}
		
		final JarEn je = new JarEn();
		je.name = srcZip.getName();
		
		try (ZipFile zf = new ZipFile(srcZip)) {
			zf.stream()
				.filter(ze->!ze.isDirectory())
				.filter(ze->ze.getName().endsWith("java"))
				.parallel()
				.forEach(ze -> {
					try {
						SourceEn src = new SourceEn(ze.getName());
						InputStream is = zf.getInputStream(ze);
						src.source = IOUtils.toString(is);
						src.jar = je;
						reMap.put(src.clzName, src);
					} catch (Exception e) {
						e.printStackTrace();
					} 
				} );
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return reMap;
	}

	@SuppressWarnings("unchecked")
	public static void main(String... args) {
		StopWatch sw = new StopWatch();
		sw.start();
		setUp();

		List<Class<?>> clzzList = new LinkedList<>();
		final Map<String, SourceEn> srcEnPool = new LinkedHashMap<String, SourceEn>();
		Stream.of(args).forEach(arg -> {
			File _f = Reflects.getJarFileInClassPath(arg);
			if (_f == null)
				_f = new File(String.format("%s/lib/rt.jar", SystemUtils.JAVA_HOME));
			
			System.out.println("load source for each class");
			srcEnPool.putAll(loadSource(_f));
			
			clzzList.addAll(Reflects.extractClazz(_f));
		} );
		Class<?>[] clzz = clzzList.toArray(new Class<?>[0]);

		EntityJpaProcessor ep = assembler();

		EntityJpaProcessor base = assembler();
		{
			ReflectDao dao = ReflectDao.threadLocal.get();
			List<ClassEn> ceList = (List<ClassEn>) dao.queryEntity("select ce from ClassEn ce");
			ceList.forEach(ce -> base.classEnPool.put(ce.name, new AtomicReference<ClassEn>(ce)));

			List<PackageEn> peList = (List<PackageEn>) dao.queryEntity("select pe from PackageEn pe");
			peList.forEach(pe -> base.packageEnPool.put(pe.name, pe));
		}

		ep.setSources(srcEnPool).preload(base, clzz).process();

		sw.stop();
		System.out.println("After " + sw.getTime());

	}
	
	final Map<String, SourceEn> srcEnPool = new LinkedHashMap<String, SourceEn>();
	
	public EntityJpaProcessor setSources(Map<String, SourceEn> _srcEnPool) {
		this.srcEnPool.putAll(_srcEnPool);
		return this;
	}
	
	@Test
	public void testMain() {
		main("rt.jar");
	}
}