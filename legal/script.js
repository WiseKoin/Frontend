(() => {
  const body = document.body;
  const header = document.querySelector("[data-header]");
  const menu = document.querySelector("[data-menu]");
  const menuToggle = document.querySelector("[data-menu-toggle]");

  const closeMenu = () => {
    if (!menu || !menuToggle) return;
    menu.classList.remove("open");
    menuToggle.setAttribute("aria-expanded", "false");
    body.classList.remove("menu-open");
  };

  menuToggle?.addEventListener("click", () => {
    const isOpen = menuToggle.getAttribute("aria-expanded") === "true";
    menuToggle.setAttribute("aria-expanded", String(!isOpen));
    menu?.classList.toggle("open", !isOpen);
    body.classList.toggle("menu-open", !isOpen);
  });

  menu?.querySelectorAll("a").forEach((link) => link.addEventListener("click", closeMenu));
  window.addEventListener("resize", () => {
    if (window.innerWidth > 980) closeMenu();
  });

  const updateHeader = () => header?.classList.toggle("scrolled", window.scrollY > 12);
  updateHeader();
  window.addEventListener("scroll", updateHeader, { passive: true });

  document.querySelectorAll("[data-current-year]").forEach((node) => {
    node.textContent = String(new Date().getFullYear());
  });

  const article = document.querySelector(".legal-document");
  const toc = document.querySelector("[data-legal-toc]");

  if (article && toc) {
    const headings = [...article.querySelectorAll("h2")];
    headings.forEach((heading, index) => {
      if (!heading.id) heading.id = `section-${index + 1}`;
      const link = document.createElement("a");
      link.href = `#${heading.id}`;
      link.textContent = heading.textContent || `Section ${index + 1}`;
      toc.appendChild(link);
    });

    if ("IntersectionObserver" in window) {
      const links = [...toc.querySelectorAll("a")];
      const observer = new IntersectionObserver(
        (entries) => {
          const visible = entries
            .filter((entry) => entry.isIntersecting)
            .sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top)[0];
          if (!visible) return;
          links.forEach((link) => {
            link.classList.toggle("active", link.getAttribute("href") === `#${visible.target.id}`);
          });
        },
        { rootMargin: "-15% 0px -70% 0px", threshold: 0 }
      );
      headings.forEach((heading) => observer.observe(heading));
    }
  }
})();
