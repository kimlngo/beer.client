package model;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beer {

    @Nullable
    private UUID id;

    @Nonnull
    private String beerName;

    @NonNull
    private String beerStyle;

    private String upc;

    private BigDecimal price;

    private Integer quantityOnHand;

    private OffsetDateTime createdDate;
    private OffsetDateTime lastUpdateDate;
}
