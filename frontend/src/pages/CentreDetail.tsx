import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Form, InputNumber } from 'antd';
import { MedicineBoxOutlined } from '@ant-design/icons';
import { useDataController } from '@helex/core';
import {
  AppCard,
  AppTag,
  AppTimestamp,
  FieldItem,
  ResourceForm,
  useAppNotification,
  type ResourceFormSection,
} from '@helex/ui';
import { centresApi, type DonationCentre, type OperatorInfo, type Region } from '../bloodApi';

/** A coordinate input: antd InputNumber with the range from BLOOD.01 § Data model. */
const CoordinateField = ({
  value,
  onChange,
  limit,
}: {
  value?: unknown;
  onChange?: (next: number | null) => void;
  limit: number;
}) => (
  <InputNumber
    style={{ width: '100%' }}
    min={-limit}
    max={limit}
    step={0.0001}
    value={value == null ? null : Number(value)}
    onChange={(v) => onChange?.(v)}
  />
);

/** Latitude and longitude come together or not at all — the same rule the backend enforces. */
const bothOrNeither = (other: 'latitude' | 'longitude') => ({
  getFieldValue,
}: {
  getFieldValue: (name: string) => unknown;
}) => ({
  validator: (_: unknown, value: unknown) =>
    (value == null) === (getFieldValue(other) == null)
      ? Promise.resolve()
      : Promise.reject(new Error('Latitude and longitude must be given together')),
});

/**
 * One donation centre on ResourceForm + useDataController — BLOOD.01 § UI,
 * Centre page. Same pattern as AnimalDetail: view mode reads the record, Edit
 * switches to inputs, Save is PUT (the centre code is identity and stays
 * read-only), Retire is the soft DELETE. The operator's legal name and address
 * come live from the business register and are omitted when it is down.
 */
export const CentreDetail = () => {
  const { id } = useParams();
  const centreId = Number(id);
  const navigate = useNavigate();
  const notify = useAppNotification();
  const [form] = Form.useForm<DonationCentre>();
  const dc = useDataController<DonationCentre>({} as DonationCentre);
  const [mode, setMode] = useState<'view' | 'edit'>('view');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [regions, setRegions] = useState<Region[]>([]);
  const [operator, setOperator] = useState<OperatorInfo | null>(null);

  const loadOperator = (centre: DonationCentre, isCancelled: () => boolean) => {
    setOperator(null);
    if (centre.operatorRegistryCode) {
      centresApi
        .operator(centre.id)
        .then((o) => !isCancelled() && setOperator(o))
        .catch(() => setOperator(null)); // register down: leave it out (fail open)
    }
  };

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    Promise.all([centresApi.get(centreId), centresApi.regions()])
      .then(([centre, list]) => {
        if (cancelled) return;
        dc.load(centre);
        form.setFieldsValue(centre);
        setRegions(list);
        loadOperator(centre, () => cancelled);
      })
      .catch((e) => {
        notify.error('Could not load the centre', e.message);
        navigate('/centres');
      })
      .finally(() => !cancelled && setLoading(false));
    return () => {
      cancelled = true;
    };
    // dc, form and navigate are stable handles; the record is keyed by the route parameter.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [centreId, notify]);

  const save = async (values: DonationCentre) => {
    setSaving(true);
    try {
      const updated = await centresApi.update(centreId, {
        ...values,
        centreCode: dc.current.centreCode,
        operatorRegistryCode: values.operatorRegistryCode || null,
        openingHours: values.openingHours || null,
        phone: values.phone || null,
      });
      dc.load(updated);
      form.setFieldsValue(updated);
      loadOperator(updated, () => false);
      setMode('view');
      notify.success('Saved', `${updated.name} · ${updated.centreCode}`);
    } catch (e) {
      notify.error('Save failed', (e as Error).message); // 400/409 problem detail, verbatim
    } finally {
      setSaving(false);
    }
  };

  const retire = async () => {
    setDeleting(true);
    try {
      await centresApi.retire(centreId);
      notify.success('Retired', `${dc.current.name} · ${dc.current.centreCode}`);
      navigate('/centres');
    } catch (e) {
      notify.error('Retire failed', (e as Error).message);
      setDeleting(false);
    }
  };

  const regionName = (code: unknown) => regions.find((r) => r.code === code)?.name ?? String(code ?? '');

  const sections: ResourceFormSection<DonationCentre>[] = [
    {
      key: 'identity',
      title: 'Identity',
      fields: [
        {
          name: 'centreCode',
          label: 'Centre code',
          type: 'text',
          modeOverride: 'view', // immutable after creation — the API ignores it on PUT
          tooltip: 'Identity does not change; retire and add again to reuse a code',
        },
        { name: 'name', label: 'Name', type: 'text', required: true, rules: [{ required: true, max: 255 }] },
        {
          name: 'regionCode',
          label: 'Region',
          type: 'select',
          required: true,
          options: regions.map((r) => ({ value: r.code, label: r.name })),
          viewRender: (code) => <AppTag>{regionName(code)}</AppTag>,
        },
      ],
    },
    {
      key: 'location',
      title: 'Location and contact',
      fields: [
        { name: 'address', label: 'Address', type: 'text', required: true, rules: [{ required: true, max: 255 }] },
        {
          name: 'latitude',
          label: 'Latitude',
          type: 'custom',
          tooltip: '−90 … 90; given together with the longitude',
          rules: [bothOrNeither('longitude')],
          render: ({ value, onChange }) => <CoordinateField value={value} onChange={onChange} limit={90} />,
          viewRender: (v) => (v == null ? '—' : String(v)),
        },
        {
          name: 'longitude',
          label: 'Longitude',
          type: 'custom',
          tooltip: '−180 … 180; given together with the latitude',
          rules: [bothOrNeither('latitude')],
          render: ({ value, onChange }) => <CoordinateField value={value} onChange={onChange} limit={180} />,
          viewRender: (v) => (v == null ? '—' : String(v)),
        },
        { name: 'openingHours', label: 'Opening hours', type: 'text', rules: [{ max: 255 }], placeholder: 'Mon–Fri 8–16' },
        { name: 'phone', label: 'Phone', type: 'text', rules: [{ max: 30 }] },
        {
          name: 'operatorRegistryCode',
          label: 'Operator code',
          type: 'text',
          rules: [{ pattern: /^\d{8}$/, message: 'Exactly 8 digits' }],
          placeholder: '90000001',
          // View mode adds what the business register knows about that code — never stored.
          viewRender: (code) =>
            code ? (
              <>
                {String(code)}
                {operator && (
                  <>
                    {' — '}
                    <b>{operator.legalName}</b>, {operator.address}
                    <div style={{ opacity: 0.65, fontSize: 12 }}>via the business register (mock / X-Road)</div>
                  </>
                )}
              </>
            ) : (
              '—'
            ),
        },
      ],
    },
  ];

  return (
    <ResourceForm<DonationCentre>
      mode={mode}
      entityLabel="Donation centre"
      icon={<MedicineBoxOutlined />}
      name={dc.current.name}
      subtitle={dc.current.centreCode}
      chips={dc.current.regionCode ? <AppTag>{regionName(dc.current.regionCode)}</AppTag> : undefined}
      loading={loading}
      saving={saving}
      deleting={deleting}
      form={form}
      dataController={dc}
      initialValues={dc.current}
      sections={sections}
      onBack={() => navigate('/centres')}
      backTooltip="Back to the list"
      onEdit={() => setMode('edit')}
      onCancel={() => {
        dc.reset();
        form.setFieldsValue(dc.original ?? dc.current);
        setMode('view');
      }}
      onSave={save}
      onDelete={retire}
      deleteLabel="Retire"
      deleteConfirmText="Retire this centre? A soft delete — the centre code becomes reusable."
      sidebar={
        <AppCard title="Metadata" size="small">
          <FieldItem label="Created at">
            <AppTimestamp value={dc.current.sysCreatedAt} fallback="—" />
          </FieldItem>
          <FieldItem label="Created by">{dc.current.sysCreatedBy ?? '—'}</FieldItem>
          <FieldItem label="Modified at">
            <AppTimestamp value={dc.current.sysModifiedAt} fallback="—" />
          </FieldItem>
          <FieldItem label="Modified by">{dc.current.sysModifiedBy ?? '—'}</FieldItem>
          <FieldItem label="Version">{dc.current.sysVersion ?? '—'}</FieldItem>
        </AppCard>
      }
    />
  );
};
