package tech.artcoded.atriangle.core.database;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@ToString
public abstract class Auditable<U> {

  @CreatedBy protected U createdBy;

  @LastModifiedBy protected U lastModifiedBy;

  @CreatedDate
  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Europe/Brussels")
  protected Date creationDate;

  @LastModifiedDate
  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Europe/Brussels")
  protected Date lastModifiedDate;

  public U getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(U createdBy) {
    this.createdBy = createdBy;
  }

  public U getLastModifiedBy() {
    return lastModifiedBy;
  }

  public void setLastModifiedBy(U lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }
}
