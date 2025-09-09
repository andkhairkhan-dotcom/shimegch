package com.example.application.base.ui;

import com.example.application.service.ConfigurationService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;

/**
 * Main application layout with top bar and navigation.
 */
@CssImport("./styles/mobile-responsive.css")
public class MainAppLayout extends AppLayout {

    private final ConfigurationService configurationService;
    private H1 viewTitle;

    public MainAppLayout(ConfigurationService configurationService) {
        this.configurationService = configurationService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.addClassNames(LumoUtility.Margin.End.SMALL);

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        // Hide view title on mobile to save space
        viewTitle.getStyle().set("display", "none");
        viewTitle.addClassNames("desktop-only");

        // Top bar text from configuration
        Span topBarText = new Span(configurationService.getTopBarText());
        topBarText.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.TextColor.PRIMARY);

        // Make top bar text responsive
        topBarText.addClassNames(LumoUtility.FontSize.SMALL);
        topBarText.getStyle().set("white-space", "nowrap");
        topBarText.getStyle().set("overflow", "hidden");
        topBarText.getStyle().set("text-overflow", "ellipsis");

        HorizontalLayout headerLayout = new HorizontalLayout(toggle, topBarText, viewTitle);
        headerLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        headerLayout.expand(topBarText); // Expand top bar text instead of view title on mobile
        headerLayout.setWidthFull();
        headerLayout.addClassNames(LumoUtility.Padding.Horizontal.SMALL);
        headerLayout.setSpacing(true);

        Header header = new Header(headerLayout);
        header.addClassNames(LumoUtility.Background.PRIMARY_10, LumoUtility.BoxShadow.SMALL);
        header.setWidthFull();

        addToNavbar(header);
    }

    private void createDrawer() {
        SideNav nav = new SideNav();

        nav.addItem(new SideNavItem("Айлуудын жагсаалт", "/"));
        nav.addItem(new SideNavItem("Хяналтын самбар", "/dashboard"));
        nav.addItem(new SideNavItem("Excel файл оруулах", "/excel-upload"));
        nav.addItem(new SideNavItem("Айлуудын удирдлага", "/households"));
        nav.addItem(new SideNavItem("Тайлан ба статистик", "/reports"));
        nav.addItem(new SideNavItem("Админ тохиргоо", "/admin-config"));

        addToDrawer(nav);
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        return getContent().getClass().getAnnotation(com.vaadin.flow.router.PageTitle.class) != null
                ? getContent().getClass().getAnnotation(com.vaadin.flow.router.PageTitle.class).value()
                : "";
    }
}
