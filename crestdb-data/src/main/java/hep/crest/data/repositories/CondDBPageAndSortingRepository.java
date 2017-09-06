/**
 * 
 * This file is part of PhysCondDB.
 *
 *   PhysCondDB is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PhysCondDB is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with PhysCondDB.  If not, see <http://www.gnu.org/licenses/>.
 **/
package hep.crest.data.repositories;

import java.io.Serializable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;


/**
 * @author formica
 *
 */
@NoRepositoryBean
public interface CondDBPageAndSortingRepository<T, ID extends Serializable> extends Repository<T, ID> {
	
	long count();
	
	boolean exists(ID id);
	
	T findOne(ID id);  

	Iterable<T> findAll();
	
	Iterable<T> findAll(Iterable<ID> ids);
	
	Page<T> findAll(Pageable pageable);
	
	Iterable<T> findAll(Sort sort);
	
}
