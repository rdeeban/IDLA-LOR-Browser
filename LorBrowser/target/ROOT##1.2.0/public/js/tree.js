// turns off asynchronization
$.ajaxSetup({
    async: false
});

// sets up d3 tree
var m = [20, 120, 20, 120],
    w = 1280 - m[1] - m[3],
    h = 800 - m[0] - m[2],
    i = 0,
    root;

var tree = d3.layout.tree()
    .size([h, w]);

var diagonal = d3.svg.diagonal()
    .projection(function(d) { return [d.y, d.x]; });

var vis = d3.select("#body").append("svg:svg")
    .attr("width", w + m[1] + m[3])
    .attr("height", h + m[0] + m[2])
    .append("svg:g")
    .attr("transform", "translate(" + (m[3]-225) + "," + m[0] + ")");

var myJson = {};
var arbor = {};

if(retrieve() != null) {
    arbor  = JSON.parse(retrieve());
    myJson = JSON.parse(JSON.stringify(arbor));
} else {
    console.log("GET /s3"+user['bucket']+user['key']);
    $.ajax({
        url: "/s3"+user['bucket']+user['key'],
        type: "GET",
        success: function(data) {
            console.log(data);
            var response = data.response;
            myJson.key = response;
        }
    });
    arbor.key = myJson.key;
    arbor.children = null;
}

root = myJson;
root.x0 = h / 2;
root.y0 = 0;

update(root);

var recent = (localStorage.getItem("recent") != null) ? localStorage.getItem("recent") : null;

function toggleAll(d) {
    /*
    if (recent != null) {
        if (recent.indexOf(d.key) == -1) {
            if (d.children) {
                d.children.forEach(toggleAll);
                toggle(d);
            }
        }
    }
    else {
    */
        if (d.children) {
            d.children.forEach(toggleAll);
            toggle(d);
        }
    //}
}

// Initialize the display to hide nodes.
root.children.forEach(toggleAll);

update(root);

function update(source) {
    var duration = d3.event && d3.event.altKey ? 5000 : 500;

    // compute the new height
    var levelWidth = [1];
    var childCount = function(level, n) {

        if(n.children && n.children.length > 0) {
            if(levelWidth.length <= level + 1) levelWidth.push(0);
            levelWidth[level+1] += n.children.length;
            n.children.forEach(function(d) {
                childCount(level + 1, d);
            });
        }
    };
    childCount(0, root);
    var newHeight = d3.max(levelWidth) * 20; // 20 pixels per line
    tree = tree.size([newHeight, w]);
    d3.select("svg").attr("height", newHeight + m[0] + m[2]);

    // Compute the new tree layout.
    var nodes = tree.nodes(root).reverse();

    // Normalize for fixed-depth.
    nodes.forEach(function(d) {
        d.y = (w/(levelWidth.length+2))*(d.depth+1);
    });

    // Update the nodes…
    var node = vis.selectAll("g.node")
        .data(nodes, function(d) { return d.id || (d.id = ++i); });

    // Enter any new nodes at the parent's previous position.
    var nodeEnter = node.enter().append("svg:g")
        .attr("class", "node")
        .attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })

    nodeEnter.append("svg:circle")
        .attr("r", 1e-6)
        .style("fill", function(d) { return d._children ? "lightsteelblue" : "#fff"; })
        .on("click", function(d) {

            refresh(d);



            toggle(d);
            update(d);
        });

    nodeEnter.append("svg:text")
        .attr("x", function(d) { return d.children || d._children ? -10 : 10; })
        .attr("dy", ".35em")
        .attr("text-anchor", function(d) { return d.children || d._children ? "end" : "start"; })
        .text(function(d) {
            var type = ((d.key.substr(d.key.length -1, d.key.length)) == "/") ? 1 : 0;
            return (d.depth == 0) ? d.key.substr(0,d.key.length-1) : (type == 1) ? d.key.split("/")[d.key.split("/").length-2] : d.key.split("/")[d.key.split("/").length-1];
        })
        .style("fill-opacity", 1e-6)
        .style("font-size", "12")
        .on("click", function(d){
            d3.selectAll("text").style("fill","").style("font-weight","").style("font-size", "12");
            d3.select(this).style("fill","green").style("font-weight","bold").style("font-size", "16");
            $("#_target").val(d.key);
            user['target'] = d.key;
            while(user['target'].indexOf("/") != -1) {
                user['target'] = user['target'].replace("/","&");
            }
            console.log("target => " + user['target']);
            $("#target").html(user['target'].split("&")[user['target'].split("&").length-2]);
            localStorage.setItem("recent", d.key);
        });

    // Transition nodes to their new position.
    var nodeUpdate = node.transition()
        .duration(duration)
        .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });

    nodeUpdate.select("circle")
        .attr("r", 4.5)
        .style("fill", function(d) {
            var type = ((d.key.substr(d.key.length -1, d.key.length)) == "/") ? 1 : 0;
            return ((d.key.split("/").length <= 2 && d.key.indexOf(".") == -1)) ? "white" : d._children ? "lightsteelblue" : (type == 1) ? "#fff" : "pink";
        });

    nodeUpdate.select("text")
        .style("fill-opacity", 1);

    // Transition exiting nodes to the parent's new position.
    var nodeExit = node.exit().transition()
        .duration(duration)
        .attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
        .remove();

    nodeExit.select("circle")
        .attr("r", 1e-6);

    nodeExit.select("text")
        .style("fill-opacity", 1e-6);

    // Update the links…
    var link = vis.selectAll("path.link")
        .data(tree.links(nodes), function(d) { return d.target.id; });

    // Enter any new links at the parent's previous position.
    link.enter().insert("svg:path", "g")
        .attr("class", "link")
        .attr("d", function(d) {
            var o = {x: source.x0, y: source.y0};
            return diagonal({source: o, target: o});
        })
        .transition()
        .duration(duration)
        .attr("d", diagonal);

    // Transition links to their new position.
    link.transition()
        .duration(duration)
        .attr("d", diagonal);

    // Transition exiting nodes to the parent's new position.
    link.exit().transition()
        .duration(duration)
        .attr("d", function(d) {
            var o = {x: source.x, y: source.y};
            return diagonal({source: o, target: o});
        })
        .remove();

    // Stash the old positions for transition.
    nodes.forEach(function(d) {
        d.x0 = d.x;
        d.y0 = d.y;
    });
}

// Toggle children.
function toggle(d) {

    if (!d.children && !d._children && d.key.indexOf(".") == -1) {
        append(d);
    }
    if (d.children) {
        d._children = d.children;
        d.children = null;
    } else {
        d.children = d._children;
        d._children = null;
    }
}

function append(d) {
    var prefix = d.key;
    while(prefix.indexOf("/") != -1) {
        prefix = prefix.replace("/","&");
    }
    var url = "/s3/children"+user['bucket']+prefix;
    var type = "GET";
    $("#feedback").html("<div class='alert alert-info'><button type='button' class='close' data-dismiss='alert' aria-hidden='true'>×</button><strong>"+type+" </strong>" + d.key + "</div>");
    console.log(type + " " + url);
    $.ajax({
        url: url,
        type: type,
        success: function(data) {
            console.log(data);

            d.children = data.response.filter(function(elem, pos) {
                return data.response.indexOf(elem) == pos;
            })

            for(var i = 0; i < d.children.length; i ++) {
                var child = {};
                child.key = d.children[i].key;
                child.children = null;
                insert(arbor,child, d);
                persist();
            }

            update(d);
            toggle(d);

        }
    });
}

function persist() {
    localStorage.setItem("sticky",JSON.stringify(arbor));
}

function retrieve() {
    return localStorage.getItem("sticky");
}

function destroy() {
    arbor = null;
    localStorage.clear();
}

function autorefresh(d) {
    if (d.children != null) {
        while (d.children.length > 0) {
            d.children.pop();
        }
        d.children = null;
    }
}

function refresh(d) {
    if ($("#refresh").prop("checked")) {
        while (d.children.length != 0) {
            d.children.pop();
        }
        d.children = null;
    }
}

function search(cur, targetkey) {
    if (cur.key == targetkey) {
        console.log(cur.key + " works with " + targetkey);
        console.log(cur);
        return cur;
    } else if (cur.children != null && cur.key.split("/") <= targetkey.split("/")) {
        var result = null;
        for (var i = 0; result == null && i < cur.children.length; i ++ ) {
            result = search(cur.children[i], targetkey);
        }
        return result;
    }
    return null;
}

function rmvChildren(cur, parent) {
    if (cur.key == parent.key) {
        cur.children = null;
    } else {
        if (cur.children != null && cur.key.split("/") <= parent.key.split("/")) {
            for (var i = 0; i < cur.children.length; i ++ ) {
                rmvChildren(cur.children[i], parent);
            }
        }
    }
}

function insert(cur, target, parent) {
    if (cur.key == parent.key) {
        if (cur.children == null) {
            cur.children = [];
        }
        var isNewChild = true;
        for (var i = 0; i < cur.children.length; i ++ ) {
            var child = cur.children[i];
            if (target.key == child.key) {
                isNewChild = false;
            }
        }
        if (isNewChild) {
            cur.children.push(target);
        }
    } else {
        if (cur.children != null && cur.key.split("/") <= target.key.split("/")) {
            for (var i = 0; i < cur.children.length; i ++ ) {
                insert(cur.children[i], target, parent);
            }
        }
    }
}

Array.prototype.remove = function() {
    var what, a = arguments, L = a.length, ax;
    while (L && this.length) {
        what = a[--L];
        while ((ax = this.indexOf(what)) !== -1) {
            this.splice(ax, 1);
        }
    }
    return this;
};




