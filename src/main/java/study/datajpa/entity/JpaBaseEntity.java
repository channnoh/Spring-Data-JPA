package study.datajpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@MappedSuperclass
public class JpaBaseEntity {

  @Column(updatable = false)
  private LocalDateTime createdDate;
  private LocalDateTime updatedDate;

  // @CreatedDate 로 간단히 사용가능
  @PrePersist
  public void PrePersist(){
    LocalDateTime now = LocalDateTime.now();
    createdDate = now;
    updatedDate = now;
  }

  // @LastModifiedDate
  @PreUpdate
  public void preUpdate(){
    updatedDate = LocalDateTime.now();
  }

}
