package csdev.couponstash.logic.parser;

import static csdev.couponstash.commons.util.DateUtil.MONTH_YEAR_VALIDATION_REGEX;
import static csdev.couponstash.model.coupon.RemindDate.DATE_FORMATTER;
import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import csdev.couponstash.commons.core.index.Index;
import csdev.couponstash.commons.util.DateUtil;
import csdev.couponstash.commons.util.StringUtil;
import csdev.couponstash.logic.parser.exceptions.ParseException;

import csdev.couponstash.model.coupon.Condition;
import csdev.couponstash.model.coupon.ExpiryDate;
import csdev.couponstash.model.coupon.Limit;
import csdev.couponstash.model.coupon.Name;
import csdev.couponstash.model.coupon.PromoCode;
import csdev.couponstash.model.coupon.RemindDate;
import csdev.couponstash.model.coupon.StartDate;
import csdev.couponstash.model.coupon.Usage;
import csdev.couponstash.model.coupon.savings.MonetaryAmount;
import csdev.couponstash.model.coupon.savings.PercentageAmount;
import csdev.couponstash.model.coupon.savings.Saveable;
import csdev.couponstash.model.coupon.savings.Savings;
import csdev.couponstash.model.tag.Tag;
/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index is not a non-zero unsigned integer.";

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String promoCode} into a {@code PromoCode}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code promoCode} is invalid.
     */
    public static PromoCode parsePromoCode(String promoCode) {
        requireNonNull(promoCode);
        String trimmedPromoCode = promoCode.trim();

        return new PromoCode(trimmedPromoCode);
    }

    /**
     * Parses a {@code String savings} into a {@code Savings}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @param savings The Collection of Strings that each represent
     *                certain savings entered by the user.
     * @param moneySymbol The symbol to be used for monetary amounts,
     *                    as specified in the UserPrefs.
     * @throws ParseException If the given {@code savings} is invalid.
     *     (if no savings are given, or if both a monetary amount
     *     and a percentage amount is given)
     */
    public static Savings parseSavings(Collection<String> savings, String moneySymbol) throws ParseException {
        requireNonNull(savings);
        boolean hasMoney = false;
        boolean hasPercent = false;
        MonetaryAmount monetaryAmount = null;
        PercentageAmount percentageAmount = null;
        List<Saveable> saveables = new ArrayList<Saveable>();
        for (String str : savings) {
            if (str.startsWith(moneySymbol)) {
                // last monetary amount will be taken
                hasMoney = true;
                String trimmedMonetaryAmount = str.trim().substring(moneySymbol.length());
                try {
                    monetaryAmount = new MonetaryAmount(Double.parseDouble(trimmedMonetaryAmount));
                } catch (NumberFormatException e) {
                    throw new ParseException(Savings.MESSAGE_CONSTRAINTS);
                } catch (IllegalArgumentException e) {
                    throw new ParseException(e.getMessage());
                }
            } else if (str.endsWith(PercentageAmount.PERCENT_SUFFIX)) {
                // last percentage amount will be taken
                hasPercent = true;
                String trimmedPercentage = str.trim();
                String rawNumber = trimmedPercentage
                        .substring(0, trimmedPercentage.length() - PercentageAmount.PERCENT_SUFFIX.length());
                try {
                    percentageAmount = new PercentageAmount(Double.parseDouble(rawNumber));
                } catch (NumberFormatException e) {
                    throw new ParseException(Savings.MESSAGE_CONSTRAINTS);
                } catch (IllegalArgumentException e) {
                    throw new ParseException(e.getMessage());
                }
            } else {
                String trimmedSaveable = str.trim();
                if (!trimmedSaveable.isBlank()) {
                    // avoid adding blank Strings
                    saveables.add(new Saveable(trimmedSaveable));
                }
            }
        }

        if ((saveables.isEmpty() && !hasMoney && !hasPercent) || (hasMoney && hasPercent)) {
            // throw an exception if no savings, or savings contains both
            // monetary amount and percentage amount
            throw new ParseException(Savings.MESSAGE_CONSTRAINTS);
        } else if (saveables.isEmpty()) {
            if (hasMoney) {
                return new Savings(monetaryAmount);
            } else {
                // hasPercent
                return new Savings(percentageAmount);
            }
        } else if (hasMoney) {
            return new Savings(monetaryAmount, saveables);
        } else if (hasPercent) {
            return new Savings(percentageAmount, saveables);
        } else {
            return new Savings(saveables);
        }
    }

    /**
     * Parses a {@code String expiryDate} into an {@code ExpiryDate}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code expiryDate} is invalid.
     */
    public static ExpiryDate parseExpiryDate(String expiryDate) throws ParseException {
        requireNonNull(expiryDate);
        String trimmedDate = expiryDate.trim();
        if (!DateUtil.isValidDate(trimmedDate)) {
            throw new ParseException(ExpiryDate.MESSAGE_CONSTRAINTS);
        }
        return new ExpiryDate(trimmedDate);
    }


    /**
     * Parses a {@code String yearMonth} into an {@code YearMonth}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code yearMonth} is invalid.
     */
    public static YearMonth parseYearMonth(String yearMonth) throws ParseException {
        requireNonNull(yearMonth);
        YearMonth ym;
        String trimmedDate = yearMonth.trim();
        try {
            if (!trimmedDate.matches(MONTH_YEAR_VALIDATION_REGEX)) {
                throw new ParseException(DateUtil.MESSAGE_YEAR_MONTH_WRONG_FORMAT);
            } else {
                ym = YearMonth.parse(trimmedDate, DateUtil.YEAR_MONTH_FORMATTER);
            }
        } catch (ParseException | DateTimeParseException pe) {
            throw new ParseException(DateUtil.MESSAGE_YEAR_MONTH_WRONG_FORMAT);
        }
        return ym;
    }

    /**
     * Parses a {@code String startDate} into an {@code StartDate}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code startDate} is invalid.
     */
    public static StartDate parseStartDate(String startDate) throws ParseException {
        requireNonNull(startDate);
        String trimmedDate = startDate.trim();
        if (!DateUtil.isValidDate(trimmedDate)) {
            throw new ParseException(StartDate.MESSAGE_CONSTRAINTS);
        }
        return new StartDate(trimmedDate);
    }

    /**
     * Parses a {@code String usage} into a {@code Usage}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code usage} is invalid.
     */
    public static Usage parseUsage(String usage) throws ParseException {
        requireNonNull(usage);
        String trimmedUsage = usage.trim();
        if (!Usage.isValidUsage(trimmedUsage)) {
            throw new ParseException(Usage.MESSAGE_CONSTRAINTS);
        }
        return new Usage(trimmedUsage);
    }

    /**
     * Parses a {@code String limit} into a {@code Limit}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code limit} is invalid.
     */
    public static Limit parseLimit(String limit) throws ParseException {
        requireNonNull(limit);
        String trimmedLimit = limit.trim();
        if (!Limit.isValidLimit(trimmedLimit)) {
            throw new ParseException(Limit.MESSAGE_CONSTRAINTS);
        }
        return new Limit(trimmedLimit);
    }

    /**
     * Parses a {@code String condition} into a {@code Condtion}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code condition} is invalid.
     */
    public static Condition parseCondition(String condition) throws ParseException {
        requireNonNull(condition);
        String trimmedCondition = condition.trim();

        if (!Condition.isValidCondition(trimmedCondition)) {
            throw new ParseException(Condition.MESSAGE_CONSTRAINTS);
        }
        return new Condition(trimmedCondition);
    }

    /**
     * Parses a {@code String monetaryAmount} into a {@code MonetaryAmount}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code originalAmount} is invalid.
     */
    public static MonetaryAmount parseMonetaryAmount(String monetaryAmount) throws ParseException {
        requireNonNull(monetaryAmount);
        String trimmedMonetaryAmount = monetaryAmount.trim();
        try {
            Double convertedMonetaryAmount = Double.parseDouble(trimmedMonetaryAmount);
            if (!MonetaryAmount.isValidMonetaryAmount(convertedMonetaryAmount)) {
                throw new ParseException(MonetaryAmount.MESSAGE_CONSTRAINTS);
            }
            return new MonetaryAmount(convertedMonetaryAmount);
        } catch (NumberFormatException nfe) {
            throw new ParseException(MonetaryAmount.MESSAGE_CONSTRAINTS);
        }
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }

    /**
     * Parses a {@code String remindDate} into a {@code RemindDate}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code remindDate} is invalid.
     */
    public static RemindDate parserRemindDate(String remindDate) throws ParseException {

        requireNonNull(remindDate);
        String trimmedDate = remindDate.trim();
        if (!RemindDate.isValidRemindDate(trimmedDate)) {
            throw new ParseException(RemindDate.MESSAGE_CONSTRAINTS);
        }
        RemindDate remind = new RemindDate();
        remind.setRemindDate(LocalDate.parse(trimmedDate, DATE_FORMATTER));
        return remind;
    }
}
