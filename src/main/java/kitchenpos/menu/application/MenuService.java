package kitchenpos.menu.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import kitchenpos.menu.application.dto.MenuCreateRequest;
import kitchenpos.menu.application.dto.MenuResponse;
import kitchenpos.menu.dao.MenuDao;
import kitchenpos.menu.dao.MenuProductDao;
import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuProduct;
import kitchenpos.menugroup.dao.MenuGroupDao;
import kitchenpos.product.dao.ProductDao;
import kitchenpos.product.domain.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuService {
    private final MenuDao menuDao;
    private final MenuGroupDao menuGroupDao;
    private final MenuProductDao menuProductDao;
    private final ProductDao productDao;

    public MenuService(
            final MenuDao menuDao,
            final MenuGroupDao menuGroupDao,
            final MenuProductDao menuProductDao,
            final ProductDao productDao
    ) {
        this.menuDao = menuDao;
        this.menuGroupDao = menuGroupDao;
        this.menuProductDao = menuProductDao;
        this.productDao = productDao;
    }

    @Transactional
    public MenuResponse create(final MenuCreateRequest dto) {
        final List<MenuProduct> menuProducts = dto.getMenuProducts();
        validateIsExistGroup(dto.getMenuGroupId());
        validateLessThanTotalPrice(menuProducts, dto.getPrice());

        final Menu menu = new Menu(dto.getName(), dto.getPrice(), dto.getMenuGroupId(), dto.getMenuProducts());
        final Menu savedMenu = menuDao.save(menu);

        final List<MenuProduct> savedMenuProducts = saveMenuProduct(menuProducts, savedMenu.getId());
        return new MenuResponse(
                savedMenu.getId(),
                savedMenu.getName(),
                savedMenu.getPrice(),
                savedMenu.getMenuGroupId(),
                savedMenuProducts
        );
    }

    private List<MenuProduct> saveMenuProduct(List<MenuProduct> menuProducts, Long menuId) {
        final List<MenuProduct> savedMenuProducts = new ArrayList<>();
        for (final MenuProduct menuProduct : menuProducts) {
            menuProduct.setMenuId(menuId);
            savedMenuProducts.add(menuProductDao.save(menuProduct));
        }
        return savedMenuProducts;
    }

    private void validateLessThanTotalPrice(List<MenuProduct> menuProducts, BigDecimal price) {
        if (price.compareTo(getMaxPrice(menuProducts)) > 0) {
            throw new IllegalArgumentException("메뉴 가격은 상품 각각의 총 가격보다 클 수 없습니다.");
        }
    }

    private BigDecimal getMaxPrice(List<MenuProduct> menuProducts) {
        BigDecimal sum = BigDecimal.ZERO;
        for (final MenuProduct menuProduct : menuProducts) {
            final Product product = productDao.findById(menuProduct.getProductId())
                    .orElseThrow(IllegalArgumentException::new);
            BigDecimal totalPrice = product.calculatePriceByQuantity(menuProduct.getQuantity());
            sum = sum.add(totalPrice);
        }
        return sum;
    }

    private void validateIsExistGroup(Long menuGroupId) {
        if (!menuGroupDao.existsById(menuGroupId)) {
            throw new IllegalArgumentException();
        }
    }

    public List<MenuResponse> list() {
        final List<Menu> menus = menuDao.findAll();
        return menus.stream()
                .map(menu -> new MenuResponse(
                        menu.getId(),
                        menu.getName(),
                        menu.getPrice(),
                        menu.getMenuGroupId(),
                        menuProductDao.findAllByMenuId(menu.getId())
                )).collect(Collectors.toList());
    }
}
