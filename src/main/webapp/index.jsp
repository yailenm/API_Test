<html>
<body>
<table>
    <tr>
        <th>POC Two Armed Robot Schedule</th>
        <th></th>
        <th>POC Two Armed Robot Schedule New Orders</th>
    </tr>
    <tr>
        <td>
        <form action="/ws_parserpdf/rest/upload" method="post" enctype="multipart/form-data">
            Choose Time Recordings file to upload<br>
            <input name="file" id="filename" type="file" /><br><br>
            Choose constraints file to upload<br>
            <input name="file2" id="const" type="file" /><br><br>
            Password<br>
            <input name="passwd" id="passwd" type="text" /><br><br>
            Number of iterations<br>
            <input name="iter" id="iter" type="number" /><br><br>
            <button name="submit" type="submit">Submit</button>
        </form>
        </td>
        <td></td>
       <td>
        <form action="/ws_parserpdf/rest/newOrders" method="post" enctype="multipart/form-data">
            Choose Time Recordings file to upload<br>
            <input name="file" id="f" type="file" /><br><br>
            Choose constraints file to upload<br>
            <input name="file2" id="con" type="file" /><br><br>
            Password<br>
            <input name="passwd" id="pass" type="text" /><br><br>
            Number of iterations<br>
            <input name="iter" id="itere" type="number" /><br><br>
            Start Time for the new orders<br>
            <input name="order" id="order" type="number" /><br><br>
            <button name="submit" type="submit">Submit</button>
       </td>
    </tr>
</form>
</table>
</body>
</html>
