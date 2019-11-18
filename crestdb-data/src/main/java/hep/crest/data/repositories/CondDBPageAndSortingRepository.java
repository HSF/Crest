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
 * Interface for pagination and sorting.
 * @param <T>
 *            the template class
 * @param <D>
 *            the template ID
 * @author formica
 *
 */
@NoRepositoryBean
public interface CondDBPageAndSortingRepository<T, D extends Serializable>
        extends Repository<T, D> {

    /**
     * @return long
     */
    long count();

    /**
     * @param id the ID class
     * @return boolean
     */
    boolean exists(D id);

    /**
     * @param id
     *            the ID
     * @return T the class
     */
    T findOne(D id);

    /**
     * @return Iterable<T>
     */
    Iterable<T> findAll();

    /**
     * @param ids
     *            the list of IDs
     * @return Iterable<T>
     */
    Iterable<T> findAll(Iterable<D> ids);

    /**
     * @param pageable the Pageable
     * @return Page<T>
     */
    Page<T> findAll(Pageable pageable);

    /**
     * @param sort
     *            the Sort
     * @return Iterable<T>
     */
    Iterable<T> findAll(Sort sort);

}
