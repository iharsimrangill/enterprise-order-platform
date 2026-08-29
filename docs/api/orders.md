# Order API

## Create order

`POST /api/v1/orders`

### Request

```json
{
  "customerId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
  "lines": [
    {
      "sku": "SKU-100",
      "quantity": 2,
      "unitPrice": 12.50
    }
  ]
}
```

### Success response

Returns `201 Created` with a `Location` header containing the new order resource path.

```json
{
  "id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "customerId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
  "status": "PENDING",
  "totalAmount": 25.00,
  "lines": [
    {
      "sku": "SKU-100",
      "quantity": 2,
      "unitPrice": 12.50,
      "subtotal": 25.00
    }
  ],
  "createdAt": "2026-08-29T15:30:00Z",
  "updatedAt": "2026-08-29T15:30:00Z"
}
```

### Validation

- `customerId` is required and must be a UUID.
- At least one line item is required.
- `sku` must not be blank.
- `quantity` must be greater than zero.
- `unitPrice` is required and cannot be negative.

Validation failures return `400 Bad Request` with structured field-level errors.

## Scope note

This PR deliberately focuses on the HTTP and application layers. Database persistence is introduced in a later PR so the API design and persistence concerns remain independently reviewable.
