package ermorg.erm.model.history;

import ermorg.erm.model.BaseModel;
import ermorg.erm.model.translation.Language;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class LanguageHistory extends BaseModel{

	@ManyToOne(cascade=CascadeType.MERGE)
	@JoinColumn(name="laguage_id")
	private Language language;
	private String languageCode;
	private String languageName;
	private String description;
	private String operation;
	
}
