package ermorg.erm.erm_command_organization.service;

import ermorg.erm.erm_command_organization.dto.requestDTO.BusinessVerticalRequest;
import ermorg.erm.erm_command_organization.dto.responseDTO.BusinessVerticalDto;
import ermorg.erm.erm_command_organization.dto.responseDTO.BusinessVerticalResponse;
import ermorg.erm.erm_command_organization.exception.InvalidDataException;
import ermorg.erm.erm_command_organization.exception.ResourceNotFoundException;

/**
 * Service interface for managing business vertical operations in the ERM system.
 * Provides methods for creating, updating, deleting, and retrieving business verticals.
 */
public interface IBusinessVerticalService {

    /**
     * Creates new business verticals based on the provided request.
     *
     * @param request the business vertical creation request containing business vertical details
     * @return BusinessVerticalResponse containing the created business vertical information
     * @throws ResourceNotFoundException if related entities (organization, company) are not found
     */
    BusinessVerticalResponse createBusinessVertical(BusinessVerticalRequest request);

    /**
     * Updates existing business verticals with the provided information.
     *
     * @param request the business vertical update request containing updated business vertical details
     * @return BusinessVerticalResponse containing the updated business vertical information
     * @throws ResourceNotFoundException if the business verticals or related entities are not found
     */
    BusinessVerticalResponse updateBusinessVertical(BusinessVerticalRequest request) throws ResourceNotFoundException;

    /**
     * Deletes a business vertical by its ID.
     *
     * @param id the ID of the business vertical to delete
     * @throws InvalidDataException if the business vertical cannot be deleted
     */
    void deleteBusinessVertical(Long id) throws InvalidDataException;

    /**
     * Retrieves a business vertical by its ID.
     *
     * @param businessVerticalId the ID of the business vertical to retrieve
     * @return BusinessVerticalDto containing the business vertical information
     * @throws ResourceNotFoundException if the business vertical is not found
     */
    BusinessVerticalDto getBusinessVertical(Long businessVerticalId) throws ResourceNotFoundException;

    /**
     * Retrieves all business verticals for a specific company.
     *
     * @param companyId the ID of the company
     * @return BusinessVerticalResponse containing the list of business verticals
     * @throws ResourceNotFoundException if the company is not found
     */
    BusinessVerticalResponse getAllBusinessVerticalsByCompanyId(Long companyId) throws ResourceNotFoundException;

    /**
     * Retrieves all business verticals for a specific department.
     *
     * @param departmentId the ID of the department
     * @return BusinessVerticalResponse containing the list of business verticals
     * @throws ResourceNotFoundException if the department is not found
     */
    BusinessVerticalResponse getAllBusinessVerticalsByDepartmentId(Long departmentId) throws ResourceNotFoundException;

    /**
     * Retrieves all business verticals for a specific business segment.
     *
     * @param businessSegmentId the ID of the business segment
     * @return BusinessVerticalResponse containing the list of business verticals
     * @throws ResourceNotFoundException if the business segment is not found or hierarchy data is incomplete
     */
    BusinessVerticalResponse getAllBusinessVerticalsByBusinessSegmentId(Long businessSegmentId) throws ResourceNotFoundException;
}
