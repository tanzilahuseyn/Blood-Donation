import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Select } from 'antd';
import { CheckSquareOutlined, PlusOutlined } from '@ant-design/icons';
import {
  AppButtonPrimary,
  AppButtonSecondary,
  AppLink,
  AppTag,
  ResourceList,
  useAppNotification,
  type ResourceListColumn,
} from '@helex/ui';
import { centresApi, type DonationCentre, type OperatorInfo, type Region } from '../bloodApi';

/**
 * The donation centre directory — BLOOD.01 § UI, Centre list. Same pattern as
 * AnimalList: ResourceList with search, a region filter, an add button, and a
 * detail panel that shows the operator fetched live from the business register.
 */
export const CentreList = () => {
  const navigate = useNavigate();
  const notify = useAppNotification();
  const [centres, setCentres] = useState<DonationCentre[]>([]);
  const [regions, setRegions] = useState<Region[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [regionCode, setRegionCode] = useState<string | undefined>();
  const [selected, setSelected] = useState<DonationCentre | null>(null);
  const [operator, setOperator] = useState<OperatorInfo | null>(null);

  useEffect(() => {
    centresApi.regions().then(setRegions).catch((e) => notify.error('Could not load regions', e.message));
  }, [notify]);

  const load = useCallback(
    (text: string, region?: string) => {
      setLoading(true);
      centresApi
        .list(text || undefined, region)
        .then((result) => setCentres(result.data))
        .catch((e) => notify.error('Could not load centres', e.message))
        .finally(() => setLoading(false));
    },
    [notify],
  );

  useEffect(() => {
    const t = setTimeout(() => load(search, regionCode), 250);
    return () => clearTimeout(t);
  }, [search, regionCode, load]);

  useEffect(() => {
    setOperator(null);
    if (selected?.id && selected.operatorRegistryCode) {
      centresApi
        .operator(selected.id)
        .then(setOperator)
        .catch(() => setOperator(null)); // register down: show nothing, not an error (fail open)
    }
  }, [selected]);

  const regionName = (code: string) => regions.find((r) => r.code === code)?.name ?? code;

  const columns: ResourceListColumn<DonationCentre>[] = [
    {
      key: 'centreCode',
      title: 'Centre code',
      dataIndex: 'centreCode',
      locked: true,
      // The identifier is the way into the record: it links to the centre's page.
      render: (code: string, centre: DonationCentre) => (
        <AppLink
          href={`/centres/${centre.id}`}
          onClick={(e) => {
            e.preventDefault();
            e.stopPropagation();
            navigate(`/centres/${centre.id}`);
          }}
        >
          {code}
        </AppLink>
      ),
    },
    { key: 'name', title: 'Name', dataIndex: 'name' },
    {
      key: 'regionCode',
      title: 'Region',
      dataIndex: 'regionCode',
      render: (code: string) => <AppTag>{regionName(code)}</AppTag>,
    },
    { key: 'address', title: 'Address', dataIndex: 'address' },
    { key: 'openingHours', title: 'Opening hours', dataIndex: 'openingHours' },
    { key: 'phone', title: 'Phone', dataIndex: 'phone', defaultVisible: false },
  ];

  return (
    <ResourceList<DonationCentre>
      title="Donation centres"
      columns={columns}
      dataSource={centres}
      rowKey="id"
      loading={loading}
      search={{ value: search, onChange: setSearch, placeholder: 'Name or address…' }}
      filter={{
        content: (
          <Select
            allowClear
            showSearch
            optionFilterProp="label"
            style={{ width: '100%' }}
            placeholder="All regions"
            value={regionCode}
            onChange={(code?: string) => setRegionCode(code)}
            options={regions.map((r) => ({ value: r.code, label: r.name }))}
          />
        ),
        onClear: () => setRegionCode(undefined),
      }}
      actions={
        <div style={{ display: 'flex', gap: 8 }}>
          <AppButtonSecondary icon={<CheckSquareOutlined />} onClick={() => navigate('/readiness')}>
            Am I ready to donate?
          </AppButtonSecondary>
          <AppButtonPrimary icon={<PlusOutlined />} onClick={() => navigate('/centres/new')}>
            Add centre
          </AppButtonPrimary>
        </div>
      }
      detailView={{
        selectedRecord: selected,
        onSelectedRecordChange: setSelected,
        title: 'Donation centre',
        render: (centre) => (
          <div style={{ display: 'grid', gap: 8 }}>
            <div>
              <b>{centre.name}</b> · {centre.centreCode}
            </div>
            <div>{centre.address}</div>
            {centre.openingHours && <div>Open: {centre.openingHours}</div>}
            {centre.phone && <div>Phone: {centre.phone}</div>}
            {centre.operatorRegistryCode && (
              <div>
                Operator: {centre.operatorRegistryCode}
                {operator && (
                  <>
                    {' — '}
                    <b>{operator.legalName}</b>, {operator.address}
                    <div style={{ opacity: 0.65, fontSize: 12 }}>via the business register (mock / X-Road)</div>
                  </>
                )}
              </div>
            )}
          </div>
        ),
      }}
    />
  );
};
