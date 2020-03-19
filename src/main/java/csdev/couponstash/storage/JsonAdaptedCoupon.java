package csdev.couponstash.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import csdev.couponstash.commons.exceptions.IllegalValueException;
import csdev.couponstash.model.coupon.Coupon;
import csdev.couponstash.model.coupon.ExpiryDate;
import csdev.couponstash.model.coupon.Limit;
import csdev.couponstash.model.coupon.Name;
import csdev.couponstash.model.coupon.Phone;
import csdev.couponstash.model.coupon.Remind;
import csdev.couponstash.model.coupon.StartDate;
import csdev.couponstash.model.coupon.Usage;
import csdev.couponstash.model.coupon.savings.PureMonetarySavings;
import csdev.couponstash.model.coupon.savings.Savings;
import csdev.couponstash.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Coupon}.
 */
class JsonAdaptedCoupon {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Coupon's %s field is missing!";

    private final String name;
    private final String phone;
    private final JsonAdaptedSavings savingsPerUse;
    private final String expiryDate;
    private final String startDate;
    private final String usage;
    private final String limit;
    private final List<JsonAdaptedTag> tagged = new ArrayList<>();
    private final JsonAdaptedPureMonetarySavings totalSaved;

    /**
     * Constructs a {@code JsonAdaptedCoupon} with the given coupon details.
     */
    @JsonCreator
    public JsonAdaptedCoupon(@JsonProperty("name") String name,
                             @JsonProperty("phone") String phone,
                             @JsonProperty("savingsPerUse") JsonAdaptedSavings savingsPerUse,
                             @JsonProperty("expiry date") String expiryDate,
                             @JsonProperty("start date") String startDate,
                             @JsonProperty("usage") String usage,
                             @JsonProperty("limit") String limit,
                             @JsonProperty("totalSaved") JsonAdaptedPureMonetarySavings totalSaved,
                             @JsonProperty("tagged") List<JsonAdaptedTag> tagged
                             ) {
        this.name = name;
        this.phone = phone;
        this.savingsPerUse = savingsPerUse;
        this.expiryDate = expiryDate;
        this.startDate = startDate;
        this.usage = usage;
        this.limit = limit;
        this.totalSaved = totalSaved;
        if (tagged != null) {
            this.tagged.addAll(tagged);
        }
    }

    /**
     * Converts a given {@code Coupon} into this class for Jackson use.
     */
    public JsonAdaptedCoupon(Coupon source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        savingsPerUse = new JsonAdaptedSavings(source.getSavingsForEachUse());
        expiryDate = source.getExpiryDate().value;
        startDate = source.getStartDate().value;
        usage = source.getUsage().value;
        limit = source.getLimit().value;
        totalSaved = new JsonAdaptedPureMonetarySavings(source.getTotalSavings());
        tagged.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted coupon object into the model's {@code Coupon} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted coupon.
     */
    public Coupon toModelType() throws IllegalValueException {
        final List<Tag> couponTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tagged) {
            couponTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (savingsPerUse == null) {
            throw new IllegalValueException(
                    String.format(MISSING_FIELD_MESSAGE_FORMAT, Savings.class.getSimpleName()));
        }
        final Savings modelSavings = savingsPerUse.toModelType();

        if (expiryDate == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    ExpiryDate.class.getSimpleName()));
        }
        if (!ExpiryDate.isValidExpiryDate(expiryDate)) {
            throw new IllegalValueException(ExpiryDate.MESSAGE_CONSTRAINTS);
        }
        final ExpiryDate modelExpiryDate = new ExpiryDate(expiryDate);

        if (startDate == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    StartDate.class.getSimpleName()));
        }
        if (!StartDate.isValidStartDate(startDate)) {
            throw new IllegalValueException(StartDate.MESSAGE_CONSTRAINTS);
        }
        final StartDate modelStartDate = new StartDate(startDate);

        if (usage == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Usage.class.getSimpleName()));
        }
        if (!Usage.isValidUsage(usage)) {
            throw new IllegalValueException(Usage.MESSAGE_CONSTRAINTS);
        }
        final Usage modelUsage = new Usage(usage);

        if (limit == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Limit.class.getSimpleName()));
        }
        if (!Limit.isValidLimit(limit)) {
            throw new IllegalValueException(Limit.MESSAGE_CONSTRAINTS);
        }
        final Limit modelLimit = new Limit(limit);

        final PureMonetarySavings modelTotalSaved;
        if (totalSaved == null) {
            // could not find totalSaved data
            // just set total savings to 0
            modelTotalSaved = new PureMonetarySavings();
        } else {
            modelTotalSaved = totalSaved.toModelType();
        }

        // TODO: change this when reminders can be saved properly
        final Remind modelRemind = new Remind();

        final Set<Tag> modelTags = new HashSet<>(couponTags);

        return new Coupon(modelName, modelPhone, modelSavings, modelExpiryDate, modelStartDate, modelUsage, modelLimit,
                modelTags, modelTotalSaved, modelRemind);

    }

}