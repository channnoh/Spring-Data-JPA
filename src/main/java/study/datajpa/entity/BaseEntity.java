package study.datajpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class) // 해당 클래스 Auditing 기능 포함
public class BaseEntity extends BaseTimeEntity {

  @CreatedBy
  @Column(updatable = false)
  private String createdBy;

  @LastModifiedBy
  private String lastModifiedBy;
}
