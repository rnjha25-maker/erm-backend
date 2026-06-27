package ermorg.erm.dto.riskDTO;

import java.util.List;

import lombok.Data;

@Data
public class ApprovalRequest {
	private Long approverId;
	private Long submitterId;
	private String pageLink;
	private String recordName;
	private String taggedMembers;
	private List<Long> recipientUserIds;
}
