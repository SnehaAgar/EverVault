
const testReplace = (val) => {
    try {
        console.log(`Testing val: ${val} (type: ${typeof val})`);
        const res = (val || 'UNKNOWN').replace('_', ' ');
        console.log(`Result 1: ${res}`);
    } catch (e) {
        console.log(`Error 1: ${e.message}`);
    }

    try {
        // Line 269 pattern
        const res = val ? val.replace('T', ' ') : 'Default';
        console.log(`Result 2: ${res}`);
    } catch (e) {
        console.log(`Error 2: ${e.message}`);
    }
}

testReplace(null);
testReplace(undefined);
testReplace("");
testReplace(0);
testReplace(false);

console.log("---");
// What if it's an object?
testReplace({});
