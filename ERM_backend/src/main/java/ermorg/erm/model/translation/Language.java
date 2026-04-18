package ermorg.erm.model.translation;

import ermorg.erm.model.BaseModel;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Language extends BaseModel {

	private String languageCode;
	private String languageName;
	private String description;

}
