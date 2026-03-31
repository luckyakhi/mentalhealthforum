package com.mentalhealthforum.forumservice.domain.model;

import java.util.UUID;

/**
 * Category aggregate.
 */
public class Category {

    private UUID id;
    private String name;
    private String description;
    private int displayOrder;
    private int threadCount;

    private Category() {}

    public static Category create(String name, String description, int displayOrder) {
        Category category = new Category();
        category.id = UUID.randomUUID();
        category.name = name;
        category.description = description;
        category.displayOrder = displayOrder;
        category.threadCount = 0;
        return category;
    }

    // Reconstitution constructor (used by persistence adapter)
    public static Category reconstitute(UUID id, String name, String description,
                                        int displayOrder, int threadCount) {
        Category category = new Category();
        category.id = id;
        category.name = name;
        category.description = description;
        category.displayOrder = displayOrder;
        category.threadCount = threadCount;
        return category;
    }

    public void incrementThreadCount() {
        this.threadCount++;
    }

    public void decrementThreadCount() {
        if (this.threadCount > 0) {
            this.threadCount--;
        }
    }

    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getDisplayOrder() { return displayOrder; }
    public int getThreadCount() { return threadCount; }
}
