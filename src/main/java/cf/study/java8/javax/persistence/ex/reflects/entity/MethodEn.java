package cf.study.java8.javax.persistence.ex.reflects.entity;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "method_en")
@Cacheable
public class MethodEn extends MemberEn {

	@ManyToOne(cascade = { CascadeType.REFRESH })
	@JoinColumn(name="return_clz_id", nullable=true)
	public ClassEn returnClass;

	@ManyToMany(cascade = { CascadeType.REFRESH })
	@JoinTable(name = "exceptions", 
						joinColumns= {@JoinColumn(name="method_en_id", referencedColumnName="id")},
	                    inverseJoinColumns = {@JoinColumn(name="exception_en_id", referencedColumnName="id")})
	public List<ClassEn> exceptionClzz = new LinkedList<ClassEn>();

	public MethodEn() {
		category = CategoryEn.METHOD;
	}

	public MethodEn(Executable m, BaseEn enclosed) {
		super(m, enclosed, CategoryEn.METHOD);
		this.method = m;
		paramsHash = hash(method);
	}
	
	public static int hash(Executable method) {
		int result = 1;
		
		if (method == null) return result;
		
		result = method.toGenericString().hashCode();
		
		return result;
	}
	
	public boolean isMatch(Executable method) {
		return (method != null) 
				&& (name.equals(method.getName()) 
				&& paramsHash == hash(method));
	}
	
	@Transient transient public Executable method;
	
	@Basic
	public int paramsHash;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + paramsHash;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof MethodEn))
			return false;
		MethodEn other = (MethodEn) obj;
		if (paramsHash != other.paramsHash)
			return false;
		return true;
	}
	
	public static MethodEn instance(ClassEn ce, Executable exe) {
		if (ce == null || exe == null) return null;
		
		Optional<BaseEn> meOpt = ce.children.stream().filter((me)->(me instanceof MethodEn && ((MethodEn)me).isMatch(exe))).findFirst();
//		if (meOpt.isPresent()) {
//			System.out.println(ce.name + ". " + exe.getName());
//		}
		MethodEn me = meOpt.isPresent() ? (MethodEn) meOpt.get() : new MethodEn(exe, ce);
		me.method = exe;
		return me;
	}
}
