// Lets institute a linked lists and then a map

// The idea is that we build a list of nodes
var node3 = {
  value: "last",
  next: null,
}

var node2 = {
  value: "middle",
  next: null,
}

var node1 = {
  value: "first",
  next: null,
}

// Now we can define some functions that will help us with a list
var first = function(node) {
  return node.value;
}

var rest = function(node) {
  return node.next;
}

// Add a new element to the beginning of the list
var cons = function(newValue, node) {
  return {
    value: newValue,
    next: node,
  };
};

// EXAMPLES
first(node1);
// => "first"
first(rest(node1));
// => "middle"
first(rest(rest(node1)));
// => "last"
var node0 = cons("new first", node1);
first(node0);
// => "new first"
first(rest(node0));
// => "first"
//
// Lets try implementing a map
var map = function(list, transform) {
  if (list === null) {
    return null;
  } else {
    // We return a list where each element of the list is the prepended transformed value
    // and recursively continue to map each successive element
    return cons(transform(first(list)), map(rest(list), transform))
  }
}
// The cool thing about this is that because this is implemented using only cons, first, and rest
// we can apply this map to any data structure that implements those functions
// For Example an Array
var first = function (array) {
  return array[0];
}

var rest = function (array) {
  var sliced = array.slice(1, array.length);
  if (sliced.length == 0) {
    return null;
  } else {
    return sliced;
  }
}

var cons = function (newValue, array) {
  return [newValue].concat(array)
}

var list = ["PA", "NY"];
map(list, function (val) { return val + " mapped!" })
