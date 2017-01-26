package cf.study.java8.javax.persistence.jpa.general;

import misc.Jsons;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by fan on 2017/1/23.
 */
@Entity
public class SimpleTestEntity implements Cloneable {
    public static final AtomicLong ID = new AtomicLong(0);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = ID.getAndIncrement();

    @Basic
    private Date updateAt = new Date();

    @Basic
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return Jsons.toString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleTestEntity)) return false;
        SimpleTestEntity that = (SimpleTestEntity) o;
        if (getId() != null && Objects.equals(getId(), that.getId())) return true;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        if (getId() != null) return Objects.hash(getId());
        return Objects.hash(getId(), getName());
    }

    public SimpleTestEntity() {}

    public SimpleTestEntity(String name) {
        this.name = name;
    }

    public SimpleTestEntity clone() {
        SimpleTestEntity ste = new SimpleTestEntity(this.name);
        ste.setUpdateAt(updateAt);
        ste.setId(id);
        return ste;
    }
}