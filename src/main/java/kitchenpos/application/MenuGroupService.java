package kitchenpos.application;

import java.util.List;
import java.util.stream.Collectors;
import kitchenpos.application.dto.request.MenuGroupCreateRequest;
import kitchenpos.application.dto.response.MenuGroupResponse;
import kitchenpos.dao.MenuGroupDao;
import kitchenpos.domain.MenuGroup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuGroupService {
    private final MenuGroupDao menuGroupDao;

    public MenuGroupService(final MenuGroupDao menuGroupDao) {
        this.menuGroupDao = menuGroupDao;
    }

    @Transactional
    public MenuGroupResponse create(final MenuGroupCreateRequest dto) {
        final MenuGroup menuGroup = new MenuGroup(dto.getName());
        final MenuGroup savedMenuGroup = menuGroupDao.save(menuGroup);
        return new MenuGroupResponse(
                savedMenuGroup.getId(),
                savedMenuGroup.getName()
        );
    }

    public List<MenuGroupResponse> list() {
        final List<MenuGroup> menuGroups = menuGroupDao.findAll();
        return menuGroups.stream()
                .map(menuGroup -> new MenuGroupResponse(
                        menuGroup.getId(),
                        menuGroup.getName())
                ).collect(Collectors.toList());
    }
}
