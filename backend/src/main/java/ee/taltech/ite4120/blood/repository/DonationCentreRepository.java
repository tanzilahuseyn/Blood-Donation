package ee.taltech.ite4120.blood.repository;

import ee.taltech.ite4120.blood.dto.CentreQueryParams;
import ee.taltech.ite4120.blood.model.DonationCentre;
import ee.taltech.ite4120.blood.model.Region;
import java.util.List;
import java.util.Map;
import org.helex.commons.db.bean.PgBeanProcessor;
import org.helex.commons.db.repo.BaseRepository;
import org.helex.commons.db.sql.SqlBuilder;
import org.helex.commons.exception.ApiClientException;
import org.helex.commons.model.QueryResult;
import org.springframework.stereotype.Repository;

/** Data access for the donation centre directory — the same shape as AnimalRepository. */
@Repository
public class DonationCentreRepository extends BaseRepository {

    private static final String SELECT = "select c.* from blood.donation_centre c ";

    /** The sort keys the list endpoint accepts (`sort=name`, `sort=-centreCode`) and their columns. */
    private static final Map<String, String> SORT = Map.of(
            "name", "c.name",
            "centreCode", "c.centre_code");

    private final PgBeanProcessor bp = new PgBeanProcessor(DonationCentre.class);
    private final PgBeanProcessor regionBp = new PgBeanProcessor(Region.class);

    public DonationCentre load(Long id) {
        return getBean(SELECT + "where c.id = ? and c.sys_status = 'A'", bp, id);
    }

    public DonationCentre loadByCentreCode(String centreCode) {
        return getBean(SELECT + "where c.centre_code = ? and c.sys_status = 'A'", bp, centreCode);
    }

    public QueryResult<DonationCentre> search(CentreQueryParams p) {
        return query(p, this::count, this::list);
    }

    private Integer count(CentreQueryParams p) {
        SqlBuilder sb = new SqlBuilder("select count(1) from blood.donation_centre c where c.sys_status = 'A'");
        sb.append(filter(p));
        return queryForObject(sb.getSql(), Integer.class, sb.getParams());
    }

    private List<DonationCentre> list(CentreQueryParams p) {
        SqlBuilder sb = new SqlBuilder(SELECT + "where c.sys_status = 'A'");
        sb.append(filter(p));
        sb.append(order(p));
        sb.append(limit(p));
        return getBeans(sb.getSql(), bp, sb.getParams());
    }

    /** An unknown sort key is the caller's fault: 400 with the allowed keys, not a 500. */
    private SqlBuilder order(CentreQueryParams p) {
        try {
            return order(p, SORT);
        } catch (IllegalArgumentException e) {
            throw new ApiClientException("unknown sort key; sortable: " + String.join(", ", SORT.keySet().stream().sorted().toList()));
        }
    }

    private SqlBuilder filter(CentreQueryParams p) {
        SqlBuilder sb = new SqlBuilder();
        if (p.getTextContains() != null && !p.getTextContains().isBlank()) {
            String like = "%" + p.getTextContains().toLowerCase() + "%";
            sb.and("(lower(c.name) like ? or lower(c.address) like ?)", like, like);
        }
        sb.appendIfNotNull("and c.region_code = ?", p.getRegionCode());
        return sb;
    }

    public Long insert(DonationCentre c) {
        String sql = """
                insert into blood.donation_centre (centre_code, name, region_code, address, latitude, longitude,
                                                   opening_hours, phone, operator_registry_code)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                returning id""";
        return queryForObject(sql, Long.class,
                c.getCentreCode(), c.getName(), c.getRegionCode(), c.getAddress(), c.getLatitude(),
                c.getLongitude(), c.getOpeningHours(), c.getPhone(), c.getOperatorRegistryCode());
    }

    public boolean update(DonationCentre c) {
        String sql = """
                update blood.donation_centre
                   set name = ?, region_code = ?, address = ?, latitude = ?, longitude = ?,
                       opening_hours = ?, phone = ?, operator_registry_code = ?
                 where id = ? and sys_status = 'A'""";
        return jdbcTemplate.update(sql, c.getName(), c.getRegionCode(), c.getAddress(), c.getLatitude(),
                c.getLongitude(), c.getOpeningHours(), c.getPhone(), c.getOperatorRegistryCode(), c.getId()) == 1;
    }

    /** Soft delete — the platform never hard-deletes registry rows. */
    public boolean retire(Long id) {
        return jdbcTemplate.update(
                "update blood.donation_centre set sys_status = 'C' where id = ? and sys_status = 'A'", id) == 1;
    }

    public List<Region> regionList() {
        return getBeans("select r.* from blood.region r order by r.name", regionBp);
    }

    public boolean regionExists(String code) {
        Integer n = queryForObject("select count(1) from blood.region where code = ?", Integer.class, code);
        return n != null && n > 0;
    }
}
