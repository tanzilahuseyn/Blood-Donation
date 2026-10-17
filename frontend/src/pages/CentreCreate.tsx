import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Form, Input, InputNumber, Select } from 'antd';
import { AppButtonPrimary, AppButtonSecondary, AppCard, useAppNotification } from '@helex/ui';
import { centresApi, type Region } from '../bloodApi';

interface CentreFormValues {
  centreCode: string;
  name: string;
  regionCode: string;
  address: string;
  latitude?: number | null;
  longitude?: number | null;
  openingHours?: string;
  phone?: string;
  operatorRegistryCode?: string;
}

/**
 * The add form — field by field BLOOD.01 § UI, Add form. Every input is an antd
 * component; the backend still checks everything and its 400/409 text is shown
 * verbatim.
 */
export const CentreCreate = () => {
  const navigate = useNavigate();
  const notify = useAppNotification();
  const [regions, setRegions] = useState<Region[]>([]);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    centresApi.regions().then(setRegions).catch((e) => notify.error('Could not load regions', e.message));
  }, [notify]);

  const submit = (values: CentreFormValues) => {
    setSaving(true);
    centresApi
      .create({
        centreCode: values.centreCode,
        name: values.name,
        regionCode: values.regionCode,
        address: values.address,
        latitude: values.latitude ?? null,
        longitude: values.longitude ?? null,
        openingHours: values.openingHours || null,
        phone: values.phone || null,
        operatorRegistryCode: values.operatorRegistryCode || null,
      })
      .then((created) => {
        notify.success('Added', `${created.name} · ${created.centreCode}`);
        navigate(`/centres/${created.id}`);
      })
      .catch((e) => notify.error('Could not add the centre', e.message)) // 400/409 problem detail, verbatim
      .finally(() => setSaving(false));
  };

  /** Latitude and longitude come together or not at all. */
  const bothOrNeither = (other: keyof CentreFormValues) => ({
    getFieldValue,
  }: {
    getFieldValue: (name: keyof CentreFormValues) => unknown;
  }) => ({
    validator: (_: unknown, value: unknown) =>
      (value == null) === (getFieldValue(other) == null)
        ? Promise.resolve()
        : Promise.reject(new Error('Latitude and longitude must be given together')),
  });

  return (
    <AppCard title="Add a donation centre" style={{ maxWidth: 640, margin: '24px auto' }}>
      <Form<CentreFormValues> layout="vertical" onFinish={submit} requiredMark>
        <Form.Item
          name="centreCode"
          label="Centre code"
          rules={[{ required: true, max: 50 }]}
          extra="Unique among active centres — e.g. TLN-01"
        >
          <Input placeholder="TLN-01" />
        </Form.Item>
        <Form.Item name="name" label="Name" rules={[{ required: true, max: 255 }]}>
          <Input />
        </Form.Item>
        <Form.Item name="regionCode" label="Region" rules={[{ required: true }]}>
          <Select
            showSearch
            optionFilterProp="label"
            options={regions.map((r) => ({ value: r.code, label: r.name }))}
            placeholder="Choose a region"
          />
        </Form.Item>
        <Form.Item name="address" label="Address" rules={[{ required: true, max: 255 }]}>
          <Input />
        </Form.Item>
        <div style={{ display: 'flex', gap: 16 }}>
          <Form.Item
            name="latitude"
            label="Latitude"
            style={{ flex: 1 }}
            dependencies={['longitude']}
            rules={[bothOrNeither('longitude')]}
          >
            <InputNumber style={{ width: '100%' }} min={-90} max={90} step={0.0001} placeholder="59.4370" />
          </Form.Item>
          <Form.Item
            name="longitude"
            label="Longitude"
            style={{ flex: 1 }}
            dependencies={['latitude']}
            rules={[bothOrNeither('latitude')]}
          >
            <InputNumber style={{ width: '100%' }} min={-180} max={180} step={0.0001} placeholder="24.7536" />
          </Form.Item>
        </div>
        <Form.Item name="openingHours" label="Opening hours" rules={[{ max: 255 }]}>
          <Input placeholder="Mon–Fri 8–16" />
        </Form.Item>
        <Form.Item name="phone" label="Phone" rules={[{ max: 30 }]}>
          <Input />
        </Form.Item>
        <Form.Item
          name="operatorRegistryCode"
          label="Operator business registry code"
          rules={[{ pattern: /^\d{8}$/, message: 'Exactly 8 digits' }]}
          extra="Try 90000001 — the imitated business register knows this organisation"
        >
          <Input placeholder="90000001" />
        </Form.Item>
        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <AppButtonSecondary onClick={() => navigate('/centres')}>Cancel</AppButtonSecondary>
          <AppButtonPrimary htmlType="submit" loading={saving}>
            Add centre
          </AppButtonPrimary>
        </div>
      </Form>
    </AppCard>
  );
};
