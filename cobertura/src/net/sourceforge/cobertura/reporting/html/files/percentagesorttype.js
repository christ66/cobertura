function percentageConverter( s )
{
	var n = s;
	var i = s.indexOf( "%" );
	if ( i != -1 )
	{
		var p1 = s.substr( 0, i );
		return parseFloat( p1 );
	}

	return parseFloat( s );
}

SortableTable.prototype.addSortType( "Percentage", percentageConverter );
