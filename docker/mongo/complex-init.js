// MongoDB 复杂 BSON 类型测试对象
// Docker 首次初始化时由 /docker-entrypoint-initdb.d 自动执行，使用 mongosh 运行。

const complex = db.getSiblingDB('demo_complex');
complex.dropDatabase();

// ---------- 复杂类型集合 ----------
complex.createCollection('complex_types');

complex.complex_types.createIndex(
    {name: 1},
    {unique: true, name: 'uk_complex_types_name'}
);
complex.complex_types.createIndex(
    {'nested.city': 1},
    {name: 'idx_complex_types_city'}
);
complex.complex_types.createIndex(
    {decimal: 1},
    {name: 'idx_complex_types_decimal'}
);

complex.complex_types.insertMany([
    {
        _id: ObjectId('64b64c4f5d1234567890ab01'),
        name: '复杂文档1',
        nested: {
            city: '上海',
            zip: NumberInt(200000),
            tags: ['a', 'b']
        },
        items: [
            {sku: 'A1', qty: NumberInt(2)},
            {sku: 'B2', qty: NumberInt(3)}
        ],
        mixed: [
            'x',
            NumberInt(7),
            true,
            ObjectId('64b64c4f5d1234567890ab11'),
            ISODate('2023-11-14T22:13:20.000Z')
        ],
        int32: NumberInt(42),
        int64: NumberLong('9223372036854770000'),
        double: 3.14159,
        decimal: NumberDecimal('1234567890.123456789012345678'),
        bool: true,
        nullValue: null,
        date: ISODate('2023-11-14T22:13:20.000Z'),
        oid: ObjectId('64b64c4f5d1234567890abcd'),
        binary: BinData(0, 'AAEC/w=='),
        regex: /^row.*/i,
        timestamp: Timestamp({t: 1700000000, i: 5}),
        minKey: MinKey(),
        maxKey: MaxKey(),
        code: Code('function(){return 1;}'),
        longText: '中文😀\nline2 \'quote\' "double" \\ slash'
    },
    {
        _id: 'string-id-2',
        name: '字符串ID',
        int32: NumberInt(7),
        nested: {level: NumberInt(2)},
        items: ['x', 'y']
    },
    {
        _id: NumberInt(3),
        name: '整数ID',
        int32: NumberInt(3),
        bool: false
    },
    {
        _id: ObjectId('64b64c4f5d1234567890ab04'),
        name: '空值文档',
        nested: null,
        decimal: null,
        date: null,
        binary: null,
        nullValue: null
    }
]);

// ---------- 第二个集合，便于测试多集合导入/导出/传输 ----------
complex.createCollection('complex_events', {
    capped: true,
    size: 1024 * 1024,
    max: 100
});

complex.complex_events.insertMany([
    {
        _id: ObjectId('64b64c4f5d1234567890ac01'),
        event: 'complex.event.created',
        source: 'docker-init',
        payload: {
            decimal: NumberDecimal('11.220000000000000001'),
            binary: BinData(0, 'AAECAwQ='),
            regex: /^event\..*/i,
            timestamp: Timestamp({t: 1700000100, i: 1}),
            tags: ['mongo', 'complex', 'init']
        },
        createdAt: ISODate('2026-01-01T00:00:00.000Z')
    },
    {
        _id: ObjectId('64b64c4f5d1234567890ac02'),
        event: 'complex.event.updated',
        source: 'docker-init',
        payload: {
            minKey: MinKey(),
            maxKey: MaxKey(),
            code: Code('function(){return "complex";}')
        },
        createdAt: ISODate('2026-01-02T00:00:00.000Z')
    },
    {
        _id: ObjectId('64b64c4f5d1234567890ac03'),
        event: 'complex.event.archived',
        source: 'docker-init',
        payload: {
            nullValue: null,
            bool: true,
            int64: NumberLong('9223372036854770001')
        },
        createdAt: ISODate('2026-01-03T00:00:00.000Z')
    }
]);

// ---------- 视图 ----------
complex.createView('v_complex_types', 'complex_types', [
    {
        $project: {
            _id: 1,
            name: 1,
            city: '$nested.city',
            decimal: 1,
            date: 1
        }
    }
]);

// ---------- 服务端函数（system.js） ----------
complex.system.js.insertOne({
    _id: 'fn_complex_add',
    value: function (a, b) {
        return a + b;
    }
});

print('MongoDB demo_complex database initialized.');
