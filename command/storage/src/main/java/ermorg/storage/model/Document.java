package ermorg.storage.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="file_storage")
@Getter
@Setter
public class Document extends BaseModel {

	@Column(name="file_name")
	private String fileName;
	
	@Column(name="file_extension")
	private String fileExtension;
	
	@Column(name="bucket_name")
	private String bucketName;
	
	@Column(name="organization_id")
	private Long organizationId;
	
	@Column(name="file_path")
	private String filePath;
	
	@Column(name="company_id")
	private Long companyId;
	
	@Column(name="content_type")
	private String contentType;
	
	@Column(name="purpose")
	private String purpose;
	
}
