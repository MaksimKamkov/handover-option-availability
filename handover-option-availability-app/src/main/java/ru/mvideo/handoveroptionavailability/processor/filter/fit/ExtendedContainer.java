package ru.mvideo.handoveroptionavailability.processor.filter.fit;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import ru.mvideo.lards.packing.algorithm.model.Container;

@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ExtendedContainer extends Container {

    @NonNull
    private String id;

    private double weight;

    public ExtendedContainer(@NonNull String id, double weight, double width, double depth, double height) {
        super(depth, width, height);
        this.id = id;
        this.weight = weight;
    }
}