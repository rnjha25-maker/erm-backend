package com.erm.erm_command_organization.service;

import com.erm.erm_command_organization.dto.requestDTO.BusinessSegmentRequest;
import com.erm.erm_command_organization.dto.responseDTO.BusinessSegmentDto;
import com.erm.erm_command_organization.dto.responseDTO.BusinessSegmentResponse;
import com.erm.erm_command_organization.exception.InvalidDataException;
import com.erm.erm_command_organization.exception.ResourceNotFoundException;

/**
 * Service interface for managing business segment operations in the ERM system.
 * Provides methods for creating, updating, deleting, and retrieving business segments.
 */
public interface IBusinessSegmentService {

    /**
     * Creates new business segments based on the provided request.
     *
     * @param request the business segment creation request containing business segment details
     * @return BusinessSegmentResponse containing the created business segment information
     * @throws ResourceNotFoundException if related entities (organization, company) are not found
     */
    BusinessSegmentResponse createBusinessSegment(BusinessSegmentRequest request);

    /**
     * Updates existing business segments with the provided information.
     *
     * @param request the business segment update request containing updated business segment details
     * @return BusinessSegmentResponse containing the updated business segment information
     * @throws ResourceNotFoundException if the business segments or related entities are not found
     */
    BusinessSegmentResponse updateBusinessSegment(BusinessSegmentRequest request) throws ResourceNotFoundException;

    /**
     * Deletes a business segment by its ID.
     *
     * @param id the ID of the business segment to delete
     * @throws InvalidDataException if the business segment cannot be deleted
     */
    void deleteBusinessSegment(Long id) throws InvalidDataException;

    /**
     * Retrieves a business segment by its ID.
     *
     * @param businessSegmentId the ID of the business segment to retrieve
     * @return BusinessSegmentDto containing the business segment information
     * @throws ResourceNotFoundException if the business segment is not found
     */
    BusinessSegmentDto getBusinessSegment(Long businessSegmentId) throws ResourceNotFoundException;

    /**
     * Retrieves all business segments for a specific company.
     *
     * @param companyId the ID of the company
     * @return BusinessSegmentResponse containing the list of business segments
     * @throws ResourceNotFoundException if the company is not found
     */
    BusinessSegmentResponse getAllBusinessSegmentsByCompanyId(Long companyId) throws ResourceNotFoundException;

    /**
     * Retrieves all business segments for a specific department.
     *
     * @param departmentId the ID of the department
     * @return BusinessSegmentResponse containing the list of business segments
     * @throws ResourceNotFoundException if the department is not found
     */
    BusinessSegmentResponse getAllBusinessSegmentsByDepartmentId(Long departmentId) throws ResourceNotFoundException;
}
