package fi.vm.yti.common.service;

import fi.vm.yti.common.dto.OrganizationDTO;
import fi.vm.yti.common.dto.ServiceCategoryDTO;
import fi.vm.yti.common.mapper.OrganizationMapper;
import fi.vm.yti.common.mapper.ServiceCategoryMapper;
import fi.vm.yti.common.repository.CommonRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import static fi.vm.yti.common.Constants.DEFAULT_LANGUAGE;

@Service
public class FrontendService {

    private final CommonRepository coreRepository;

    public FrontendService(CommonRepository coreRepository) {
        this.coreRepository = coreRepository;
    }

    public List<OrganizationDTO> getOrganizations(String sortLanguage, boolean includeChildOrganizations) {
        var organizations = coreRepository.getOrganizations();
        var dtos = OrganizationMapper.mapToListOrganizationDTO(organizations);

        dtos.sort((a, b) -> {
            var labelA = a.getLabel().getOrDefault(sortLanguage, a.getLabel().get(DEFAULT_LANGUAGE));
            var labelB = b.getLabel().getOrDefault(sortLanguage, b.getLabel().get(DEFAULT_LANGUAGE));
            return labelA.compareTo(labelB);
        });

        return includeChildOrganizations ? dtos : dtos.stream()
                .filter(dto -> dto.getParentOrganization() == null)
                .toList();
    }

    public List<ServiceCategoryDTO> getServiceCategories() {
        return getServiceCategories(DEFAULT_LANGUAGE);
    }

    public List<ServiceCategoryDTO> getServiceCategories(String sortLanguage) {
        var serviceCategories = coreRepository.getServiceCategories();
        var dtos = ServiceCategoryMapper.mapToListServiceCategoryDTO(serviceCategories);

        dtos.sort((a, b) -> {
            var labelA = a.getLabel().getOrDefault(sortLanguage, a.getLabel().get(DEFAULT_LANGUAGE));
            var labelB = b.getLabel().getOrDefault(sortLanguage, b.getLabel().get(DEFAULT_LANGUAGE));
            return labelA.compareTo(labelB);
        });

        return dtos;
    }
}
