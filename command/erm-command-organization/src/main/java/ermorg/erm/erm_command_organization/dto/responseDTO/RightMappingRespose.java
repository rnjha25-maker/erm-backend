package ermorg.erm.erm_command_organization.dto.responseDTO;

import ermorg.erm.erm_command_organization.model.RoleRight;

import lombok.Data;

@Data
public class RightMappingRespose {

	private long rightId;
	private boolean view;
	private boolean create;
	private boolean update;
	private boolean delete;
	private boolean export;
	private boolean print;
	private boolean approve;
	private boolean reject;
	private boolean cancel;
	
	public RightMappingRespose(RoleRight roleRight) {
		this.rightId = roleRight.getRight().getId();
		this.view = roleRight.getView();
		this.create = roleRight.getCreate();
		this.update = roleRight.getUpdate();
		this.delete = roleRight.getDelete();
		this.export = roleRight.getExport();
		this.print = roleRight.getPrint();
		this.approve = roleRight.getApprove();
		this.reject = roleRight.getReject();
		this.cancel = roleRight.getCancel();
	}
}
