package ermorg.erm.erm_command_organization.dto.requestDTO;

import lombok.Data;

@Data
public class RightMappingRequest {
	private long rightId;
	private boolean map;
	private boolean view;
	private boolean create;
	private boolean update;
	private boolean delete;
	private boolean export;
	private boolean print;
	private boolean approve;
	private boolean reject;
	private boolean cancel;

}
