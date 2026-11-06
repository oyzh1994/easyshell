// MongoDB 测试对象
// Docker 首次初始化时由 /docker-entrypoint-initdb.d 自动执行，使用 mongosh 运行

const demo = db.getSiblingDB('demo');
demo.dropDatabase();

// ---------- 用户集合 ----------
demo.createCollection('users', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['name', 'email', 'active', 'createdAt'],
            properties: {
                name: {bsonType: 'string', description: '用户名'},
                email: {bsonType: 'string', description: '邮箱'},
                age: {bsonType: 'int', minimum: 0},
                balance: {bsonType: ['double', 'int', 'long', 'decimal']},
                active: {bsonType: 'bool'},
                profile: {bsonType: 'object'},
                createdAt: {bsonType: 'date'}
            }
        }
    },
    validationLevel: 'moderate',
    validationAction: 'error'
});

demo.users.createIndex({email: 1}, {unique: true, name: 'uk_users_email'});
demo.users.createIndex({name: 1}, {name: 'idx_users_name'});
demo.users.createIndex({'profile.level': 1}, {name: 'idx_users_profile_level'});

demo.users.insertMany([
    {
        _id: 1,
        name: 'alice',
        email: 'a@example.com',
        age: NumberInt(20),
        balance: 10.50,
        active: true,
        profile: {level: NumberInt(1), tags: ['vip']},
        createdAt: new Date('2026-01-01T00:00:00Z')
    },
    {
        _id: 2,
        name: 'bob',
        email: 'b@example.com',
        age: NumberInt(30),
        balance: 20.00,
        active: false,
        profile: {level: NumberInt(2), tags: ['normal']},
        createdAt: new Date('2026-01-02T00:00:00Z')
    }
]);

// ---------- 订单集合 ----------
demo.createCollection('orders', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['orderNo', 'userId', 'amount', 'status'],
            properties: {
                orderNo: {bsonType: 'string'},
                userId: {bsonType: 'int'},
                amount: {bsonType: ['double', 'int', 'long', 'decimal']},
                status: {bsonType: 'string'},
                createdAt: {bsonType: 'date'}
            }
        }
    },
    validationAction: 'error'
});

demo.orders.createIndex({orderNo: 1}, {unique: true, name: 'uk_orders_order_no'});
demo.orders.createIndex({userId: 1}, {name: 'idx_orders_user_id'});

demo.orders.insertMany([
    {
        _id: 1,
        orderNo: 'ORD-0001',
        userId: NumberInt(1),
        amount: 99.00,
        status: 'PAID',
        createdAt: new Date('2026-01-03T10:00:00Z')
    },
    {
        _id: 2,
        orderNo: 'ORD-0002',
        userId: NumberInt(2),
        amount: 20.00,
        status: 'NEW',
        createdAt: new Date('2026-01-03T11:00:00Z')
    }
]);

// ---------- 视图 ----------
demo.createView('v_users', 'users', [
    {$project: {_id: 1, name: 1, email: 1, age: 1, active: 1}}
]);

// ---------- 固定集合，模拟日志/事件 ----------
demo.createCollection('logs', {capped: true, size: 1024 * 1024, max: 1000});
demo.logs.insertOne({level: 'INFO', message: 'demo initialized', createdAt: new Date()});

// ---------- 计数器，模拟序列 ----------
demo.createCollection('counters');
demo.counters.insertOne({_id: 'order', seq: NumberLong(2)});

// ---------- 服务端函数（system.js） ----------
demo.system.js.insertOne({
    _id: 'fn_add',
    value: function (a, b) {
        return a + b;
    }
});

print('MongoDB demo database initialized.');
